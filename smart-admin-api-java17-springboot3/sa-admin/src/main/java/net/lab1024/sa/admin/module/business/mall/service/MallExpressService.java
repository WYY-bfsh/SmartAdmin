package net.lab1024.sa.admin.module.business.mall.service;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.mall.config.MallProperties;
import net.lab1024.sa.admin.module.business.mall.constant.ExpressCompanyEnum;
import net.lab1024.sa.admin.module.business.mall.dao.MallExpressTraceDao;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallExpressTraceEntity;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallOrderEntity;
import net.lab1024.sa.admin.module.business.mall.domain.vo.ExpressCompanyVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallExpressTraceVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物流：公司编码对齐快递100；配置了 customer+key 则查真实轨迹，否则按发货时间推进同结构演示节点。
 */
@Slf4j
@Service
public class MallExpressService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private MallExpressTraceDao mallExpressTraceDao;

    @Resource
    private MallProperties mallProperties;

    public List<ExpressCompanyVO> companies() {
        List<ExpressCompanyVO> list = new ArrayList<>();
        for (ExpressCompanyEnum item : ExpressCompanyEnum.values()) {
            ExpressCompanyVO vo = new ExpressCompanyVO();
            vo.setCode(item.getValue());
            vo.setName(item.getDesc());
            list.add(vo);
        }
        return list;
    }

    public void initShipTraces(MallOrderEntity order) {
        replaceTraces(order.getOrderId(), demoNodes(order, 0));
    }

    public List<MallExpressTraceVO> refreshAndList(MallOrderEntity order) {
        if (order.getShipTime() == null || StringUtils.isBlank(order.getWaybillNo())) {
            return List.of();
        }
        if (kuaidi100Ready()) {
            List<MallExpressTraceVO> remote = queryKuaidi100(order.getExpressCode(), order.getWaybillNo());
            if (!remote.isEmpty()) {
                replaceTraces(order.getOrderId(), remote);
                return remote;
            }
        }
        long minutes = Math.max(0, Duration.between(order.getShipTime(), LocalDateTime.now()).toMinutes());
        List<MallExpressTraceVO> demo = demoNodes(order, minutes);
        replaceTraces(order.getOrderId(), demo);
        return demo;
    }

    public List<MallExpressTraceVO> list(Long orderId) {
        List<MallExpressTraceEntity> list = mallExpressTraceDao.selectList(new LambdaQueryWrapper<MallExpressTraceEntity>()
                .eq(MallExpressTraceEntity::getOrderId, orderId)
                .orderByDesc(MallExpressTraceEntity::getSortNo)
                .orderByDesc(MallExpressTraceEntity::getTraceId));
        return list.stream().map(item -> {
            MallExpressTraceVO vo = new MallExpressTraceVO();
            vo.setFtime(item.getFtime());
            vo.setContext(item.getContext());
            vo.setStatusText(item.getStatusText());
            return vo;
        }).collect(Collectors.toList());
    }

    public boolean kuaidi100Ready() {
        return StringUtils.isNotBlank(mallProperties.getExpress().getKuaidi100Key())
                && StringUtils.isNotBlank(mallProperties.getExpress().getKuaidi100Customer());
    }

    private List<MallExpressTraceVO> queryKuaidi100(String com, String num) {
        try {
            String key = mallProperties.getExpress().getKuaidi100Key();
            String customer = mallProperties.getExpress().getKuaidi100Customer();
            JSONObject param = new JSONObject();
            param.put("com", com);
            param.put("num", num);
            param.put("resultv2", "1");
            String paramStr = param.toJSONString();
            String sign = DigestUtil.md5Hex(paramStr + key + customer).toUpperCase();
            Map<String, Object> form = new HashMap<>();
            form.put("customer", customer);
            form.put("sign", sign);
            form.put("param", paramStr);
            String body = HttpUtil.post("https://poll.kuaidi100.com/poll/query.do", form, 8000);
            JSONObject json = JSON.parseObject(body);
            JSONArray data = json.getJSONArray("data");
            if (data == null || data.isEmpty()) {
                return List.of();
            }
            List<MallExpressTraceVO> list = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JSONObject row = data.getJSONObject(i);
                MallExpressTraceVO vo = new MallExpressTraceVO();
                vo.setFtime(row.getString("ftime"));
                vo.setContext(row.getString("context"));
                vo.setStatusText(row.getString("status"));
                list.add(vo);
            }
            return list;
        } catch (Exception e) {
            log.warn("快递100查询失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 压缩时间轴便于联调：真实快递节点名称与快递100一致，生产请改用接口时间。
     */
    private List<MallExpressTraceVO> demoNodes(MallOrderEntity order, long minutes) {
        String company = StringUtils.defaultIfBlank(order.getExpressName(), "快递");
        String city = extractCity(order.getReceiverAddress());
        LocalDateTime ship = order.getShipTime() == null ? LocalDateTime.now() : order.getShipTime();
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0, "已发货", "卖家已发货，等待揽收。运单号 " + order.getWaybillNo()));
        if (minutes >= 1) {
            nodes.add(new Node(1, "揽收", company + "已揽收，正发往分拨中心"));
        }
        if (minutes >= 3) {
            nodes.add(new Node(3, "在途", "快件已离开【华东转运中心】，发往【" + city + "】"));
        }
        if (minutes >= 6) {
            nodes.add(new Node(6, "在途", "快件已到达【" + city + "转运中心】"));
        }
        if (minutes >= 8) {
            nodes.add(new Node(8, "派件", "【" + city + "】派件员正在派件，请保持电话畅通"));
        }
        if (minutes >= 12) {
            nodes.add(new Node(12, "签收", "快件已签收，签收人：门卫/家人。感谢使用" + company));
        }
        List<MallExpressTraceVO> list = new ArrayList<>();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            MallExpressTraceVO vo = new MallExpressTraceVO();
            vo.setFtime(ship.plusMinutes(node.offset).format(FMT));
            vo.setContext(node.context);
            vo.setStatusText(node.status);
            list.add(vo);
        }
        return list;
    }

    private void replaceTraces(Long orderId, List<MallExpressTraceVO> traces) {
        mallExpressTraceDao.delete(new LambdaQueryWrapper<MallExpressTraceEntity>()
                .eq(MallExpressTraceEntity::getOrderId, orderId));
        int sort = traces.size();
        for (MallExpressTraceVO trace : traces) {
            MallExpressTraceEntity entity = new MallExpressTraceEntity();
            entity.setOrderId(orderId);
            entity.setFtime(trace.getFtime());
            entity.setContext(trace.getContext());
            entity.setStatusText(trace.getStatusText());
            entity.setSortNo(sort--);
            entity.setCreateTime(LocalDateTime.now());
            mallExpressTraceDao.insert(entity);
        }
    }

    private String extractCity(String address) {
        if (StringUtils.isBlank(address)) {
            return "目的地";
        }
        String compact = address.replace(" ", "");
        int idx = compact.indexOf("市");
        if (idx > 0 && idx < 8) {
            return compact.substring(0, idx + 1);
        }
        return compact.length() > 4 ? compact.substring(0, 4) : compact;
    }

    private record Node(int offset, String status, String context) {
    }
}
