package com.btl.bookstore.service.impl;

import com.btl.bookstore.service.CommonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/*
Service xử lý các chức năng chung
Quản lý message session và định dạng tiền tệ
*/
@Service
public class CommonServiceImpl implements CommonService {

    @Value("${currency.sign}")
    public String currencySign;

    // Xóa message trong session (succMsg, errorMsg)
    @Override
    public void removeSessionMessage() {
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.getRequestAttributes()))
                .getRequest();
        HttpSession session = request.getSession();
        session.removeAttribute("succMsg");
        session.removeAttribute("errorMsg");
    }

    @Override
    public String currencySign() {
        return currencySign;
    }
}
