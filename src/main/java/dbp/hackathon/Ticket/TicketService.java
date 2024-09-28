package dbp.hackathon.Ticket;

import dbp.hackathon.Estudiante.Estudiante;
import dbp.hackathon.Estudiante.EstudianteRepository;
import dbp.hackathon.Funcion.Funcion;
import dbp.hackathon.Funcion.FuncionRepository;
import dbp.hackathon.email.domain.EmailService;
import jakarta.mail.MessagingException;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.EscapedState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private FuncionRepository funcionRepository;

    public Ticket createTicket(Long estudianteId, Long funcionId, Integer cantidad) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId).orElse(null);
        Funcion funcion = funcionRepository.findById(funcionId).orElse(null);
        if (estudiante == null || funcion == null) {
            throw new IllegalStateException("Estudiante or Funcion not found!");
        }

        Ticket ticket = new Ticket();
        ticket.setEstudiante(estudiante);
        ticket.setFuncion(funcion);
        ticket.setCantidad(cantidad);
        ticket.setEstado(Estado.VENDIDO);
        ticket.setFechaCompra(LocalDateTime.now());
        ticket.setQr("GENERATED-QR-CODE");

        return ticketRepository.save(ticket);
    }

    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        ticketRepository.deleteById(id);
    }

    public Iterable<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Iterable<Ticket> findByEstudianteId(Long estudianteId) {
        return ticketRepository.findByEstudianteId(estudianteId);
    }

    public void changeState(Long id) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            throw new IllegalStateException("Ticket not found!");
        }
        ticket.setEstado(Estado.CANJEADO);
        ticketRepository.save(ticket);
    }

    @Autowired
    private EmailService emailService;

    public void enviarConfirmacionReserva(String emailDestino, Long estudianteid, Long funcion_id, String qrCode) {
        Context context = new Context();
        Funcion funcion = funcionRepository.findById(funcion_id).orElse(null);
        Estudiante estudiante = estudianteRepository.findById(estudianteid).orElse(null);
        context.setVariable("nombre", estudiante.getName());
        context.setVariable("nombrePelicula", funcion.getNombre());
        context.setVariable("fechaFuncion", funcion.getFecha().toString());
        context.setVariable("cantidadEntradas", funcion.getStock());
        context.setVariable("precioTotal", funcion.getPrecio().toString());
        context.setVariable("qr", qrCode);

        try {
            emailService.sendEmail(emailDestino, "Confirmación de Reserva", "confirmacion-reserva", context);
        } catch (MessagingException e) {
            // Manejar la excepción
        }
    }
}
