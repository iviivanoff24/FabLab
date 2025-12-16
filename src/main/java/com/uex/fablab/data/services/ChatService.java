package com.uex.fablab.data.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uex.fablab.data.model.ChatMessage;
import com.uex.fablab.data.model.ChatResponse;
import com.uex.fablab.data.model.Course;
import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.Product;
import com.uex.fablab.data.repository.CourseRepository;
import com.uex.fablab.data.repository.MachineRepository;
import com.uex.fablab.data.repository.ProductRepository;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;

@Service
public class ChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final MachineRepository machineRepository;
    private final CourseRepository courseRepository;
    private final ProductRepository productRepository;

    public ChatService(ChatLanguageModel chatLanguageModel, MachineRepository machineRepository, CourseRepository courseRepository, ProductRepository productRepository) {
        this.chatLanguageModel = chatLanguageModel;
        this.machineRepository = machineRepository;
        this.courseRepository = courseRepository;
        this.productRepository = productRepository;
    }

    public ChatResponse processMessage(ChatMessage chatMessage) {
        try {
            // Obtener datos actualizados de la base de datos
            List<Machine> machines = machineRepository.findAll();
            List<Course> courses = courseRepository.findAll();
            List<Product> products = productRepository.findAll();

            String machinesInfo = machines.stream()
                .map(m -> String.format("- %s (Estado: %s): %s", m.getName(), m.getStatus(), m.getDescription()))
                .collect(Collectors.joining("\n"));

            String coursesInfo = courses.stream()
                .map(c -> String.format("- %s (De %s a %s): %s", c.getName(), c.getStartDate(), c.getEndDate(), c.getDescription()))
                .collect(Collectors.joining("\n"));

            String productsInfo = products.stream()
                .map(p -> String.format("- %s: %s", p.getName(), p.getDescription()))
                .collect(Collectors.joining("\n"));

            String userMessageText = chatMessage.getMessage();
            String systemPrompt = "Eres el asistente virtual del Fablab Mérida (Universidad de Extremadura). " +
                    "Tu objetivo es ayudar a los estudiantes y usuarios con dudas sobre el Fablab. " +
                    "Información clave: " +
                    "- Ubicación: Calle Santa Teresa de Jornet, 38. Mérida. " +
                    "- Horario: Lunes a Viernes de 9:00 a 21:00. " +
                    "- Contacto: fablab@uex.es, 613 006 794. " +
                    "- Normas: Reserva obligatoria, uso de EPIs, limpieza obligatoria. " +
                    "- Precios: Consultar en la web (sección Máquinas). " +
                    "- Reservas: Se hacen desde la web, iniciando sesión. " +
                    "\n\nINFORMACIÓN ACTUALIZADA DE LA BASE DE DATOS:\n" +
                    "MÁQUINAS:\n" + machinesInfo + "\n\n" +
                    "CURSOS:\n" + coursesInfo + "\n\n" +
                    "PRODUCTOS/MATERIALES:\n" + productsInfo + "\n\n" +
                    "Sé amable, lo mas claro, breve, y conciso que puedad, y útil. Responde en el idioma que se utilice en el mensaje del usuario.";

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
