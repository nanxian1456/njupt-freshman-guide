package cn.xszn.comments.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class ClientIdentityService {
  public String getClientIp(HttpServletRequest request) {
    String remoteAddress = request.getRemoteAddr();
    if (isLoopback(remoteAddress)) {
      String realIp = request.getHeader("X-Real-IP");
      if (realIp != null && realIp.matches("[0-9a-fA-F:.]{3,45}")) {
        return realIp;
      }
    }
    return remoteAddress == null ? "unknown" : remoteAddress;
  }

  private boolean isLoopback(String address) {
    return "127.0.0.1".equals(address) || "::1".equals(address) || "0:0:0:0:0:0:0:1".equals(address);
  }
}
