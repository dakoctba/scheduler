package com.jacto.scheduler.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
@Tag(name = "Teste", description = "Endpoints para testar a autenticação")
public class TestController {
    @GetMapping("/all")
    @Operation(
        summary = "Acesso público",
        description = "Endpoint que pode ser acessado sem autenticação"
    )
    public String allAccess() {
        return "Conteúdo Público.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    @Operation(
        summary = "Acesso de usuário",
        description = "Endpoint que pode ser acessado apenas por usuários autenticados com role USER ou ADMIN",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Acesso permitido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
        }
    )
    public String userAccess() {
        return "Conteúdo de Usuário.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Acesso de administrador",
        description = "Endpoint que pode ser acessado apenas por usuários autenticados com role ADMIN",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Acesso permitido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
        }
    )
    public String adminAccess() {
        return "Conteúdo de Administrador.";
    }
}
