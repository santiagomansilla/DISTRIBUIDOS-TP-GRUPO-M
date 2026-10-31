package com.unla.tp_distribuidos.dto;

public class RegistroResponseDto {

     private String username;
     private Long usuarioId;
     private String mensaje;
     
     public String getUsername() {
         return username;
     }
     public void setUsername(String username) {
         this.username = username;
     }
     public Long getUsuarioId() {
         return usuarioId;
     }
     public void setUsuarioId(Long usuarioId) {
         this.usuarioId = usuarioId;
     }
     public String getMensaje() {
         return mensaje;
     }
     public void setMensaje(String mensaje) {
         this.mensaje = mensaje;
     }
    
}
