package net.lab1024.sa.admin.module.business.mall.handler;

import net.lab1024.sa.admin.module.business.mall.service.MallMemberService;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MallExceptionHandler {

    @ExceptionHandler(MallMemberService.MallLoginException.class)
    public ResponseDTO<?> handleLogin(MallMemberService.MallLoginException e) {
        return ResponseDTO.error(UserErrorCode.LOGIN_STATE_INVALID);
    }
}
