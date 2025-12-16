package com.uex.fablab.data.services;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Service;
import com.uex.fablab.data.model.ChatMessage;
import com.uex.fablab.data.model.ChatResponse;

@Service
public class ChatService {

    private final ChatLanguageModel chatLanguageModel;

    public ChatService(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    public ChatResponse processMessage(ChatMessage chatMessage) {
        try {
            String userMessageText = chatMessage.getMessage();
            String systemPrompt = "Eres el asistente virtual del Fablab Mérida (Universidad de Extremadura). " +
                    "Tu objetivo es ayudar a los estudiantes y usuarios con dudas sobre el Fablab. " +
                    "Información clave: " +
                    "- Ubicación: Calle Santa Teresa de Jornet, 38. Mérida. " +
                    "- Horario: Lunes a Viernes de 9:00 a 21:00. " +
                    "- Contacto: fablab@uex.es, 613 006 794. " +
                    "- Máquinas: Impresoras 3D, Cortadoras Láser, Fresadoras CNC, Vinilo. " +
                    "- Normas: Reserva obligatoria, uso de EPIs, limpieza obligatoria. " +
                    "- Precios: Consultar en la web (sección Máquinas). " +
                    "- Reservas: Se hacen desde la web, iniciando sesión. " +
                    "Sé amable, conciso y útil. Responde siempre en español.";

            Response<AiMessage> response = chatLanguageModel.generate(
                SystemMessage.from(systemPrompt),
                UserMessage.from(userMessageText)
            );

            return new ChatResponse(response.content().text());

        } catch (Exception e) {
            return new ChatResponse("Error interno del servidor: " + e.getMessage());
        }
    }
}
