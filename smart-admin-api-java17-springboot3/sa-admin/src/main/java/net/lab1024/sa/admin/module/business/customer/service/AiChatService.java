package net.lab1024.sa.admin.module.business.customer.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.customer.constant.QAStatusEnum;
import net.lab1024.sa.admin.module.business.customer.dao.KnowledgeDao;
import net.lab1024.sa.admin.module.business.customer.dao.QADao;
import net.lab1024.sa.admin.module.business.customer.domain.entity.KnowledgeEntity;
import net.lab1024.sa.admin.module.business.customer.domain.entity.QAEntity;
import net.lab1024.sa.admin.module.business.customer.domain.form.AiChatHistoryForm;
import net.lab1024.sa.admin.module.business.customer.domain.form.AiChatSendForm;
import net.lab1024.sa.admin.module.business.customer.domain.vo.AiChatHistoryVO;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.redis.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 客服 AI 问答。流式调用百炼兼容接口，同一个话题的历史放在现有 Redis。
 * 这台机器内存很小，同时打到模型的请求限制为 4 个。
 */
@Slf4j
@Service
public class AiChatService {

    private static final String CONTEXT_PREFIX = "ai:chat:ctx:";
    private static final String HISTORY_PREFIX = "ai:chat:history:";
    private static final long EXPIRE_SECONDS = 7 * 24 * 3600L;
    private static final int MAX_HISTORY = 10;
    private static final Semaphore IN_FLIGHT = new Semaphore(4);

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    @Value("${ai.chat.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${ai.chat.api-key:}")
    private String apiKey;

    @Value("${ai.chat.model:qwen-plus}")
    private String model;

    @Resource
    private RedisService redisService;

    @Resource
    private KnowledgeDao knowledgeDao;

    @Resource
    private QADao qaDao;

    public SseEmitter send(AiChatSendForm form) {
        Long userId = AdminRequestUtil.getRequestUserId();
        String topicId = form.getTopicId() == null || form.getTopicId().isBlank()
                ? UUID.randomUUID().toString().replace("-", "")
                : form.getTopicId().trim();
        boolean useContext = form.getUseContext() == null || Boolean.TRUE.equals(form.getUseContext());
        SseEmitter emitter = new SseEmitter(120_000L);
        AtomicBoolean closed = new AtomicBoolean(false);
        emitter.onCompletion(() -> closed.set(true));
        emitter.onTimeout(() -> closed.set(true));
        emitter.onError(ex -> closed.set(true));
        CompletableFuture.runAsync(() -> stream(userId, topicId, form.getMessage().trim(), useContext, emitter, closed));
        return emitter;
    }

    public ResponseDTO<String> saveHistory(AiChatHistoryForm form) {
        redisService.set(historyKey(AdminRequestUtil.getRequestUserId()), form.getContent(), EXPIRE_SECONDS);
        return ResponseDTO.ok();
    }

    public ResponseDTO<AiChatHistoryVO> getHistory() {
        AiChatHistoryVO vo = new AiChatHistoryVO();
        vo.setContent(redisService.get(historyKey(AdminRequestUtil.getRequestUserId())));
        return ResponseDTO.ok(vo);
    }

    private void stream(Long userId, String topicId, String message, boolean useContext, SseEmitter emitter, AtomicBoolean closed) {
        boolean acquired = false;
        try {
            emitTopic(emitter, topicId);
            if (apiKey == null || apiKey.isBlank()) {
                finish(emitter, "还没有配置 AI 密钥。请在服务器环境变量 AI_CHAT_API_KEY 中填入百炼密钥后，重启后端。");
                return;
            }
            acquired = IN_FLIGHT.tryAcquire();
            if (!acquired) {
                finish(emitter, "当前提问较多，请稍后再试。");
                return;
            }
            List<JSONObject> history = useContext ? loadContext(userId, topicId) : new ArrayList<>();
            history.add(messageOf("user", message));
            String answer = callModel(history, emitter, closed);
            if (!closed.get()) {
                history.add(messageOf("assistant", answer));
                saveContext(userId, topicId, history);
                emitDone(emitter);
            }
        } catch (Exception e) {
            log.error("AI 问答失败 topicId={}", topicId, e);
            if (!closed.get()) {
                try {
                    finish(emitter, "回答失败，请稍后再试。");
                } catch (Exception ignored) {
                    emitter.completeWithError(e);
                }
            }
        } finally {
            if (acquired) {
                IN_FLIGHT.release();
            }
        }
    }

    private String callModel(List<JSONObject> history, SseEmitter emitter, AtomicBoolean closed) throws Exception {
        String question = history.get(history.size() - 1).getString("content");
        JSONArray messages = new JSONArray();
        messages.add(messageOf("system", systemPrompt(question)));
        int from = Math.max(0, history.size() - MAX_HISTORY);
        for (int i = from; i < history.size(); i++) {
            messages.add(history.get(i));
        }
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("stream", true);
        body.put("messages", messages);
        String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(90))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<java.util.stream.Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
        if (response.statusCode() != 200) {
            log.warn("AI 接口返回 {}", response.statusCode());
            String tip = response.statusCode() == 401 ? "AI 密钥无效，请检查 AI_CHAT_API_KEY。" : "AI 接口暂时不可用，请稍后再试。";
            emitContent(emitter, tip);
            return tip;
        }
        StringBuilder answer = new StringBuilder();
        try (java.util.stream.Stream<String> lines = response.body()) {
            lines.forEach(line -> {
                if (closed.get()) {
                    return;
                }
                String data = dataOf(line);
                if (data == null || "[DONE]".equals(data)) {
                    return;
                }
                String piece = contentOf(data);
                if (piece == null || piece.isEmpty()) {
                    return;
                }
                answer.append(piece);
                try {
                    emitContent(emitter, piece);
                } catch (Exception e) {
                    closed.set(true);
                }
            });
        }
        return answer.toString();
    }

    private String systemPrompt(String question) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是客服中心的问答助手，用简体中文直接回答。不要编造订单号、金额或政策。");
        prompt.append("资料里没有答案时，说明暂时没有对应资料，并建议用户到工单管理提交问题。\n");
        List<String> refs = retrieve(question);
        if (!refs.isEmpty()) {
            prompt.append("可参考的资料：\n");
            refs.forEach(ref -> prompt.append(ref).append('\n'));
        }
        return prompt.toString();
    }

    private List<String> retrieve(String question) {
        List<Scored> scored = new ArrayList<>();
        if (question == null || question.isBlank()) {
            return List.of();
        }
        List<KnowledgeEntity> knowledgeList = knowledgeDao.selectList(new LambdaQueryWrapper<KnowledgeEntity>()
                .eq(KnowledgeEntity::getDeletedFlag, false)
                .last("limit 200"));
        for (KnowledgeEntity item : knowledgeList) {
            String plain = strip(item.getContent());
            int score = score(question, item.getTitle(), plain);
            if (score > 0) {
                scored.add(new Scored(score, "知识《" + item.getTitle() + "》：" + cut(plain, 500)));
            }
        }
        List<QAEntity> qaList = qaDao.selectList(new LambdaQueryWrapper<QAEntity>()
                .eq(QAEntity::getDeletedFlag, false)
                .eq(QAEntity::getStatus, QAStatusEnum.ANSWERED.getValue())
                .last("limit 200"));
        for (QAEntity item : qaList) {
            int score = score(question, item.getQuestion(), item.getAnswer());
            if (score > 0) {
                scored.add(new Scored(score, "问答：" + item.getQuestion() + " -> " + cut(item.getAnswer(), 400)));
            }
        }
        scored.sort(Comparator.comparingInt(Scored::score).reversed());
        List<String> refs = new ArrayList<>();
        for (int i = 0; i < Math.min(3, scored.size()); i++) {
            refs.add(scored.get(i).text());
        }
        return refs;
    }

    private int score(String question, String title, String body) {
        String q = question == null ? "" : question;
        String t = title == null ? "" : title;
        String b = body == null ? "" : body;
        int score = 0;
        if (!t.isEmpty() && (q.contains(t) || t.contains(q))) {
            score += 20;
        }
        for (int i = 0; i < q.length() - 1; i++) {
            String gram = q.substring(i, i + 2);
            if (gram.isBlank()) {
                continue;
            }
            if (t.contains(gram)) {
                score += 3;
            } else if (b.contains(gram)) {
                score += 1;
            }
        }
        return score;
    }

    private void saveContext(Long userId, String topicId, List<JSONObject> history) {
        int from = Math.max(0, history.size() - MAX_HISTORY);
        redisService.set(contextKey(userId, topicId), JSON.toJSONString(history.subList(from, history.size())), EXPIRE_SECONDS);
    }

    private List<JSONObject> loadContext(Long userId, String topicId) {
        String raw = redisService.get(contextKey(userId, topicId));
        List<JSONObject> list = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return list;
        }
        JSONArray array = JSON.parseArray(raw);
        if (array == null) {
            return list;
        }
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            if (item != null && item.getString("content") != null) {
                list.add(item);
            }
        }
        return list;
    }

    private JSONObject messageOf(String role, String content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content == null ? "" : content);
        return message;
    }

    private void finish(SseEmitter emitter, String text) throws Exception {
        emitContent(emitter, text);
        emitDone(emitter);
    }

    private void emitTopic(SseEmitter emitter, String topicId) throws Exception {
        JSONObject data = new JSONObject();
        data.put("topicId", topicId);
        emitter.send(SseEmitter.event().data(data.toJSONString()));
    }

    private void emitContent(SseEmitter emitter, String content) throws Exception {
        JSONObject data = new JSONObject();
        data.put("content", content);
        emitter.send(SseEmitter.event().data(data.toJSONString()));
    }

    private void emitDone(SseEmitter emitter) throws Exception {
        emitter.send(SseEmitter.event().data("[DONE]"));
        emitter.complete();
    }

    private String dataOf(String line) {
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        if (!trimmed.startsWith("data:")) {
            return null;
        }
        return trimmed.substring(5).trim();
    }

    private String contentOf(String data) {
        try {
            JSONObject json = JSON.parseObject(data);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            JSONObject choice = choices.getJSONObject(0);
            JSONObject delta = choice.getJSONObject("delta");
            if (delta != null && delta.getString("content") != null) {
                return delta.getString("content");
            }
            JSONObject message = choice.getJSONObject("message");
            return message == null ? null : message.getString("content");
        } catch (Exception e) {
            return null;
        }
    }

    private String strip(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]+>", " ").replace("&nbsp;", " ").replaceAll("\\s+", " ").trim();
    }

    private String cut(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    private String contextKey(Long userId, String topicId) {
        return CONTEXT_PREFIX + userId + ":" + topicId;
    }

    private String historyKey(Long userId) {
        return HISTORY_PREFIX + userId;
    }

    private record Scored(int score, String text) {
    }
}
