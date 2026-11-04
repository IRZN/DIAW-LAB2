package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.PedidoDTO.AvaliacaoRequest;
import br.pucminas.aluguelcarros.dto.PedidoDTO.ContratoRequest;
import br.pucminas.aluguelcarros.dto.PedidoDTO.PedidoRequest;
import br.pucminas.aluguelcarros.dto.PedidoDTO.PedidoResponse;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import br.pucminas.aluguelcarros.security.UsuarioLogado;
import br.pucminas.aluguelcarros.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Pedidos de aluguel: introdução, modificação, consulta, cancelamento, avaliação e contrato. */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** Cliente: lista os próprios pedidos. Agente: lista todos (filtro opcional por status). */
    @GetMapping
    public List<PedidoResponse> listar(@RequestParam(required = false) StatusPedido status,
                                       @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.listar(UsuarioLogado.de(jwt), status);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.buscar(id, UsuarioLogado.de(jwt));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@Valid @RequestBody PedidoRequest request, @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.criar(request, UsuarioLogado.de(jwt));
    }

    /** Cliente (pedidos PENDENTES) e agentes (PENDENTES ou APROVADOS) podem modificar. */
    @PutMapping("/{id}")
    public PedidoResponse atualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.atualizar(id, request, UsuarioLogado.de(jwt));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('CLIENTE')")
    public PedidoResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.cancelar(id, UsuarioLogado.de(jwt));
    }

    @PatchMapping("/{id}/avaliar")
    @PreAuthorize("hasRole('AGENTE')")
    public PedidoResponse avaliar(@PathVariable Long id, @Valid @RequestBody AvaliacaoRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.avaliar(id, request, UsuarioLogado.de(jwt));
    }

    @PostMapping("/{id}/contrato")
    @PreAuthorize("hasRole('AGENTE')")
    public PedidoResponse executarContrato(@PathVariable Long id, @Valid @RequestBody ContratoRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.executarContrato(id, request, UsuarioLogado.de(jwt));
    }
}
