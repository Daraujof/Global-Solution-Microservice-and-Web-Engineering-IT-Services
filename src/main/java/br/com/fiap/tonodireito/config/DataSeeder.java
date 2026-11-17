package br.com.fiap.tonodireito.config;

import br.com.fiap.tonodireito.entity.Direito;
import br.com.fiap.tonodireito.entity.Documento;
import br.com.fiap.tonodireito.entity.enums.CategoriaDireito;
import br.com.fiap.tonodireito.entity.enums.EsferaGoverno;
import br.com.fiap.tonodireito.repository.DireitoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Carga inicial com alguns direitos reais, só quando a tabela está vazia. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final DireitoRepository direitoRepository;

    public DataSeeder(DireitoRepository direitoRepository) {
        this.direitoRepository = direitoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (direitoRepository.count() > 0) {
            return;
        }
        LocalDate revisao = LocalDate.of(2026, 11, 5);

        Direito bpc = direito("BPC - 1 salário mínimo por mês",
                "Um salário mínimo por mês, pago pelo INSS, para pessoa idosa (65+) ou com deficiência de família com renda baixa. Não precisa ter contribuído.",
                CategoriaDireito.DINHEIRO_BENEFICIO, EsferaGoverno.FEDERAL,
                "https://www.planalto.gov.br/ccivil_03/leis/l8742.htm", 65, revisao);
        doc(bpc, "Inscrição atualizada no CadÚnico", "Feita ou atualizada nos últimos 2 anos", true, "CRAS do bairro", 24);
        doc(bpc, "Documento com foto e CPF", "De todas as pessoas da casa", true, "Poupatempo", null);
        doc(bpc, "Laudos médicos", "Somente se o pedido for por deficiência", false, "UBS / médico que acompanha", null);

        Direito tarifa = direito("Tarifa Social de Energia Elétrica",
                "Desconto na conta de luz para famílias do CadÚnico com renda de até meio salário mínimo por pessoa ou com pessoa que recebe BPC.",
                CategoriaDireito.DESCONTO_TARIFA, EsferaGoverno.FEDERAL,
                "https://www.planalto.gov.br/ccivil_03/_ato2007-2010/2010/lei/l12212.htm", null, revisao);
        doc(tarifa, "Número do NIS", "Número de Identificação Social do responsável familiar", true, "CRAS ou app CadÚnico", null);
        doc(tarifa, "Conta de luz recente", "Para informar o número da instalação", true, "Distribuidora de energia", null);

        Direito carteira = direito("Carteira da Pessoa Idosa",
                "Garante vagas gratuitas ou com 50% de desconto no transporte interestadual para pessoa idosa de baixa renda.",
                CategoriaDireito.TRANSPORTE, EsferaGoverno.FEDERAL,
                "https://www.planalto.gov.br/ccivil_03/leis/2003/l10.741.htm", 60, revisao);
        doc(carteira, "Inscrição no CadÚnico", "Com renda de até 2 salários mínimos", true, "CRAS do bairro", 24);

        direitoRepository.save(bpc);
        direitoRepository.save(tarifa);
        direitoRepository.save(carteira);
        log.info("Carga inicial: 3 direitos e 6 documentos cadastrados.");
    }

    private Direito direito(String nome, String descricao, CategoriaDireito cat, EsferaGoverno esfera,
                            String fonte, Integer idadeMin, LocalDate revisao) {
        Direito d = new Direito();
        d.setNome(nome);
        d.setDescricao(descricao);
        d.setCategoria(cat);
        d.setEsfera(esfera);
        d.setFonteOficialUrl(fonte);
        d.setIdadeMinima(idadeMin);
        d.setDataRevisao(revisao);
        d.setAtivo(true);
        return d;
    }

    private void doc(Direito direito, String nome, String descricao, boolean obrigatorio, String onde, Integer validade) {
        Documento doc = new Documento();
        doc.setNome(nome);
        doc.setDescricao(descricao);
        doc.setObrigatorio(obrigatorio);
        doc.setOndeEmitir(onde);
        doc.setValidadeMeses(validade);
        doc.setDireito(direito);
        direito.getDocumentos().add(doc);
    }
}
