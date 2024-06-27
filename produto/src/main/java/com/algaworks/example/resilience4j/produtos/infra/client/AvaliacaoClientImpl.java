package com.algaworks.example.resilience4j.produtos.infra.client;

import com.algaworks.example.resilience4j.produtos.CircuitBreakerLogConfig;
import com.algaworks.example.resilience4j.produtos.client.avaliacoes.AvaliacaoClient;
import com.algaworks.example.resilience4j.produtos.client.avaliacoes.AvaliacaoModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Component
public class AvaliacaoClientImpl implements AvaliacaoClient {

	private final Logger logger = LoggerFactory.getLogger(AvaliacaoClientImpl.class);
	private final RestTemplate restTemplate;
	private final CircuitBreakerLogConfig cbLogConfig;
	
	private final static String API_URL = UriComponentsBuilder
			.fromHttpUrl("http://localhost:8091/avaliacoes")
			.queryParam("produtoId", "{produtoId}")
			.encode()
			.toUriString();
	
	public AvaliacaoClientImpl(RestTemplate restTemplate, CircuitBreakerLogConfig cbLogConfig) {
		this.restTemplate = restTemplate;
        this.cbLogConfig = cbLogConfig;
    }

	@Override
	@CircuitBreaker(name = "avaliacaoCB", fallbackMethod = "buscarTodosPorProdutoErro")
	public List<AvaliacaoModel> buscarTodosPorProduto(Long produtoId) {
		final List<AvaliacaoModel> avaliacoes = executarRequisicao(produtoId);
		return avaliacoes;
	}

	private List<AvaliacaoModel> executarRequisicao(Long produtoId) {
		final Map<String, Object> parametros = new HashMap<>();
		parametros.put("produtoId", produtoId);

		logger.info("Buscando avaliações");
		final AvaliacaoModel[] avaliacoes;
		
		try {
			avaliacoes = restTemplate.getForObject(API_URL, AvaliacaoModel[].class, parametros);
		} catch (Exception e) {
			logger.error("Erro ao buscar avaliações");
			throw e;
		}
		cbLogConfig.cbLog();

		return Arrays.asList(avaliacoes);
	}

	public List<AvaliacaoModel> buscarTodosPorProdutoErro(Long produtoId, Throwable e) {
		return new ArrayList<>();
	}
}
