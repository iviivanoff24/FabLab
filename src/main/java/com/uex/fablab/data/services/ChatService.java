package com.uex.fablab.data.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uex.fablab.data.model.Booking;
import com.uex.fablab.data.model.ChatMessage;
import com.uex.fablab.data.model.ChatResponse;
import com.uex.fablab.data.model.Course;
import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.Product;
import com.uex.fablab.data.repository.BookingRepository;
import com.uex.fablab.data.repository.CourseRepository;
import com.uex.fablab.data.repository.MachineRepository;
import com.uex.fablab.data.repository.ProductRepository;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;

/**
 * Servicio de chat con asistente virtual.
 * Utiliza un modelo de lenguaje para responder preguntas sobre el FabLab.
 */
@Service
public class ChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final MachineRepository machineRepository;
    private final CourseRepository courseRepository;
    private final ProductRepository productRepository;
    private final BookingRepository bookingRepository;

    /**
     * Constructor.
     * @param chatLanguageModel modelo de lenguaje
     * @param machineRepository repositorio de máquinas
     * @param courseRepository repositorio de cursos
     * @param productRepository repositorio de productos
     * @param bookingRepository repositorio de reservas
     */
    public ChatService(ChatLanguageModel chatLanguageModel, MachineRepository machineRepository, CourseRepository courseRepository, ProductRepository productRepository, BookingRepository bookingRepository) {
        this.chatLanguageModel = chatLanguageModel;
        this.machineRepository = machineRepository;
        this.courseRepository = courseRepository;
        this.productRepository = productRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Procesa un mensaje de usuario y genera una respuesta.
     * Incluye contexto actualizado de la base de datos en el prompt del sistema.
     *
     * @param chatMessage mensaje del usuario
     * @return respuesta generada
     */
    public ChatResponse processMessage(ChatMessage chatMessage) {
        try {
            // Obtener datos actualizados de la base de datos
            List<Machine> machines = machineRepository.findAll();
            List<Course> courses = courseRepository.findAll();
            List<Product> products = productRepository.findAll();
            List<Booking> bookings = bookingRepository.findAll();

            String machinesInfo = machines.stream()
                .map(m -> String.format("- %s (Estado: %s): %s", m.getName(), m.getStatus(), m.getDescription()))
                .collect(Collectors.joining("\n"));

            String coursesInfo = courses.stream()
                .map(c -> String.format("- %s (De %s a %s): %s", c.getName(), c.getStartDate(), c.getEndDate(), c.getDescription()))
                .collect(Collectors.joining("\n"));

            String productsInfo = products.stream()
                .map(p -> String.format("- %s: %s", p.getName(), p.getDescription()))
                .collect(Collectors.joining("\n"));

            String bookingsInfo = bookings.stream()
                .map(b -> String.format("- Reserva de %s el %s (%s-%s). Estado: %s", 
                    b.getShift().getMachine().getName(),
                    b.getShift().getDate(),
                    b.getShift().getStartTime(),
                    b.getShift().getEndTime(),
                    b.getEstado()))
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
                    "RESERVAS EXISTENTES:\n" + bookingsInfo + "\n\n" +
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
