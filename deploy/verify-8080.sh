#!/bin/bash
for i in 1 2 3 4 5 6 7 8 9 10 12 14; do
  code=$(curl -sS -m 4 -o /tmp/cap.json -w '%{http_code}' http://127.0.0.1:8080/api/login/getCaptcha || echo 000)
  if [ "$code" = "200" ] && grep -q captchaBase64 /tmp/cap.json 2>/dev/null; then
    echo CAPTCHA_OK try_$i
    break
  fi
  echo try_${i}_$code
  sleep 4
done
curl -sS -m 5 -o /dev/null -w 'home %{http_code}\n' http://127.0.0.1:8080/
curl -sS -m 5 -o /dev/null -w 'app %{http_code}\n' http://127.0.0.1:8080/app/
