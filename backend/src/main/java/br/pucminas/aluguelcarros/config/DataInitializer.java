package br.pucminas.aluguelcarros.config;

import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Rendimento;
import br.pucminas.aluguelcarros.model.enums.TipoAgente;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Popula o banco com usuários e automóveis de demonstração quando ele está vazio.
 * Desative com {@code APP_SEED_ENABLED=false}.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String SENHA_PADRAO = "123456";

    private final UsuarioRepository usuarioRepository;
    private final AutomovelRepository automovelRepository;
    private final PasswordEncoder encoder;
    private final boolean habilitado;

    public DataInitializer(UsuarioRepository usuarioRepository, AutomovelRepository automovelRepository,
                           PasswordEncoder encoder, @Value("${app.seed.enabled:true}") boolean habilitado) {
        this.usuarioRepository = usuarioRepository;
        this.automovelRepository = automovelRepository;
        this.encoder = encoder;
        this.habilitado = habilitado;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!habilitado || usuarioRepository.count() > 0) {
            return;
        }
        String senha = encoder.encode(SENHA_PADRAO);

        usuarioRepository.save(new Agente("locadora", senha, "Locadora Minas Rent", "12.345.678/0001-90", TipoAgente.EMPRESA));
        usuarioRepository.save(new Agente("banco", senha, "Banco Horizonte S.A.", "98.765.432/0001-10", TipoAgente.BANCO));

        Cliente cliente = new Cliente("cliente", senha, "MG-12.345.678", "123.456.789-09",
                "Maria Silva", "Rua da Bahia, 1000 - Centro, Belo Horizonte/MG", "Analista de Sistemas");
        cliente.definirRendimentos(List.of(
                new Rendimento("Tech Minas Ltda.", new BigDecimal("7500.00")),
                new Rendimento("Freelance", new BigDecimal("1800.00"))));
        usuarioRepository.save(cliente);

        if (automovelRepository.count() == 0) {
            automovelRepository.saveAll(List.of(
                    new Automovel("00123456789", 2024, "Volkswagen", "Polo Highline", "RTA1B23"),
                    new Automovel("00987654321", 2023, "Fiat", "Pulse Drive", "QWE4C56"),
                    new Automovel("00456789123", 2025, "Toyota", "Corolla Cross XRE", "HJK7D89"),
                    new Automovel("00321654987", 2022, "Chevrolet", "Onix LT", "PLM2E34"),
                    new Automovel("00741852963", 2024, "Hyundai", "Creta Comfort", "BHZ5F67")));
        }
        log.info("Dados de demonstração criados. Logins: cliente / locadora / banco — senha: {}", SENHA_PADRAO);
    }
}
