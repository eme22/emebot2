package com.eme22.bolo.dto;

import com.eme22.bolo.language.LanguageService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReport {
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    @Builder.Default
    private List<String> updatedFields = new ArrayList<>();
    private boolean valid;

    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void addUpdatedField(String fieldName) {
        this.updatedFields.add(fieldName);
    }

    public String toFormattedReport(LanguageService lang) {
        StringBuilder sb = new StringBuilder();
        String title = lang != null ? lang.getMessage("validation.report.title") : "Reporte de Validación de Configuración";
        sb.append("📋 **").append(title).append("**\n\n");

        if (!errors.isEmpty()) {
            String errHeader = lang != null ? lang.getMessage("validation.report.errors") : "Errores Críticos:";
            sb.append("❌ **").append(errHeader).append("**\n");
            for (String error : errors) {
                sb.append("• ").append(error).append("\n");
            }
            sb.append("\n");
        }

        if (!warnings.isEmpty()) {
            String warnHeader = lang != null ? lang.getMessage("validation.report.warnings") : "Advertencias (Recursos no encontrados en Discord):";
            sb.append("⚠️ **").append(warnHeader).append("**\n");
            for (String warning : warnings) {
                sb.append("• ").append(warning).append("\n");
            }
            sb.append("\n");
        }

        if (!updatedFields.isEmpty()) {
            String fieldsHeader = lang != null ? lang.getMessage("validation.report.fields", updatedFields.size()) : "Campos a actualizar/mergear: (" + updatedFields.size() + " campos)";
            sb.append("✅ **").append(fieldsHeader).append("**\n");
            sb.append(String.join(", ", updatedFields)).append("\n");
        }

        return sb.toString();
    }
}
