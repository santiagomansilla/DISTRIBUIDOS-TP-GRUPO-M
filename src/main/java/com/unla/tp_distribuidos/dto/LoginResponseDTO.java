package com.unla.tp_distribuidos.dto;

public class LoginResponseDTO {
    private String username;
    private String rol;
    private Long clienteId;
    private String token;

    public LoginResponseDTO(String username, String rol, Long clienteId, String token) {
        this.username = username;
        this.rol = rol;
        this.clienteId = clienteId;
        this.token = token;
    }

    public String getUsername() { return username; }
    public String getRol() { return rol; }
    public Long getClienteId() { return clienteId; }
    public String getToken() { return token; }
}