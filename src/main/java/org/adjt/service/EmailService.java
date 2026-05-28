package org.adjt.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.adjt.dto.AvaliacoesPorDiaDTO;
import org.adjt.dto.AvaliacoesPorUrgenciaDTO;
import org.adjt.dto.FeedbackResumoDTO;
import org.adjt.dto.RelatorioSemanalDTO;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.logging.Logger;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "relatorio.email.destinatario")
    String emailDestinatario;

    public void enviarRelatorioSemanal(RelatorioSemanalDTO relatorio) {
        String html = buildHtml(relatorio);
        String assunto = String.format("Relatório Semanal de Feedbacks — %s a %s",
                relatorio.periodoInicio().toLocalDate(),
                relatorio.periodoFim().toLocalDate());

        mailer.send(Mail.withHtml(emailDestinatario, assunto, html));
        LOG.info("Relatório semanal enviado para " + emailDestinatario);
    }

    private String buildHtml(RelatorioSemanalDTO r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;color:#333'>");
        sb.append("<h1 style='color:#004080'>Relatório Semanal de Feedbacks</h1>");

        sb.append("<table style='border-collapse:collapse;margin-bottom:16px'>");
        row(sb, "Período", r.periodoInicio().toLocalDate() + " a " + r.periodoFim().toLocalDate());
        row(sb, "Gerado em", r.dataGeracao().toString());
        row(sb, "Total de avaliações", String.valueOf(r.totalAvaliacoes()));
        row(sb, "Média das notas", String.format("%.2f", r.mediaNotas()));
        sb.append("</table>");

        sb.append("<h2>Avaliações por Urgência</h2>");
        sb.append("<table border='1' cellpadding='6' style='border-collapse:collapse'>");
        sb.append("<tr style='background:#004080;color:#fff'><th>Urgência</th><th>Quantidade</th><th>Percentual</th></tr>");
        for (AvaliacoesPorUrgenciaDTO u : r.avaliacoesPorUrgencia()) {
            String cor = switch (u.urgencia()) {
                case CRITICA -> "#ffe0e0";
                case MEDIA   -> "#fff8e0";
                case BOA     -> "#e0ffe0";
            };
            sb.append("<tr style='background:").append(cor).append("'>")
              .append("<td>").append(u.urgencia()).append("</td>")
              .append("<td>").append(u.quantidade()).append("</td>")
              .append(String.format("<td>%.1f%%</td>", u.percentual()))
              .append("</tr>");
        }
        sb.append("</table>");

        sb.append("<h2>Avaliações por Dia</h2>");
        sb.append("<table border='1' cellpadding='6' style='border-collapse:collapse'>");
        sb.append("<tr style='background:#004080;color:#fff'><th>Data</th><th>Quantidade</th></tr>");
        for (AvaliacoesPorDiaDTO d : r.avaliacoesPorDia()) {
            sb.append("<tr><td>").append(d.data()).append("</td><td>").append(d.quantidade()).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<h2>Detalhes dos Feedbacks</h2>");
        sb.append("<table border='1' cellpadding='6' style='border-collapse:collapse'>");
        sb.append("<tr style='background:#004080;color:#fff'><th>Data de Envio</th><th>Nota</th><th>Urgência</th><th>Descrição</th></tr>");
        for (FeedbackResumoDTO f : r.feedbacks()) {
            sb.append("<tr><td>").append(f.dataEnvio()).append("</td>")
              .append("<td>").append(f.nota()).append("</td>")
              .append("<td>").append(f.urgencia()).append("</td>")
              .append("<td>").append(f.descricao()).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private void row(StringBuilder sb, String label, String value) {
        sb.append("<tr><td style='font-weight:bold;padding:4px 12px 4px 0'>")
          .append(label).append(":</td><td>").append(value).append("</td></tr>");
    }
}