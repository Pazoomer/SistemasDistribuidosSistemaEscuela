import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;

public class AppCalificaciones extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;
    private JButton btnCalificaciones;
    private JButton btnMaestros;
    private JButton btnMaestrosFaltantes;
    private JTable table;
    private String jwtToken = "";
    private List<Map<String, Object>> listaCalificaciones;
    private List<Map<String, Object>> listaMaestros;

    public AppCalificaciones() {
        setTitle("Login y Calificaciones");
        setSize(1000, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior con login y botones
        JPanel topPanel = new JPanel(new FlowLayout());

        // Campos de login
        userField = new JTextField(10);
        passField = new JPasswordField(10);
        loginButton = new JButton("Iniciar sesión");

        // Botones de acciones
         btnCalificaciones = new JButton("Mostrar calificaciones");
         btnMaestros = new JButton("Mostrar maestros");
         btnMaestrosFaltantes = new JButton("Maestros sin calificaciones");

        topPanel.add(new JLabel("Usuario:"));
        topPanel.add(userField);
        topPanel.add(new JLabel("Contraseña:"));
        topPanel.add(passField);
        topPanel.add(loginButton);
        topPanel.add(btnCalificaciones);
        topPanel.add(btnMaestros);
        topPanel.add(btnMaestrosFaltantes);

        add(topPanel, BorderLayout.NORTH);

        // Tabla
        table = new JTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Acción de login
        loginButton.addActionListener(e -> hacerLogin());

        btnCalificaciones.addActionListener(e -> {
            if (!jwtToken.isEmpty()) obtenerCalificaciones();
            else JOptionPane.showMessageDialog(this, "Primero inicia sesión.");
        });

        btnMaestros.addActionListener(e -> {
            if (!jwtToken.isEmpty()) obtenerMaestros();
            else JOptionPane.showMessageDialog(this, "Primero inicia sesión.");
        });

        btnMaestrosFaltantes.addActionListener(e -> {
            if (!jwtToken.isEmpty()) mostrarMaestrosFaltantesEnTabla();
            else JOptionPane.showMessageDialog(this, "Primero inicia sesión.");
        });

    }

    private void hacerLogin() {
        String usuario = userField.getText();
        String contrasena = new String(passField.getPassword());

        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = String.format("{\"usuario\":\"%s\",\"password\":\"%s\"}", usuario, contrasena);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/login"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> jsonToken = mapper.readValue(response.body(), new TypeReference<>() {});
                jwtToken = jsonToken.get("token");
                JOptionPane.showMessageDialog(this, "Sesion iniciada.");
                //obtenerCalificaciones();
                //obtenerMaestros();
            } else {
                JOptionPane.showMessageDialog(this, "Error de login: " + response.statusCode());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al hacer login");
        }
    }

    private void obtenerCalificaciones() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/calificacion"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                listaCalificaciones = mapper.readValue(
                        response.body(), new TypeReference<List<Map<String, Object>>>() {}
                );
                mostrarEnTabla(listaCalificaciones);
            } else {
                System.out.println(response);
                JOptionPane.showMessageDialog(this, "Error al obtener calificaciones: " + response.statusCode());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al obtener calificaciones");
        }
    }

    private void obtenerMaestros() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/maestros"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                listaMaestros = mapper.readValue(
                        response.body(), new TypeReference<List<Map<String, Object>>>() {}
                );
                mostrarEnTabla(listaMaestros); // Primero muestra los maestros

                //mostrarMaestrosFaltantesEnTabla(); // Luego muestra los maestros sin calificaciones
            } else {
                System.out.println(response);
                JOptionPane.showMessageDialog(this, "Error al obtener maestros: " + response.statusCode());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al obtener maestros");
        }
    }

    private void mostrarEnTabla(List<Map<String, Object>> calificaciones) {
        if (calificaciones.isEmpty()) return;

        DefaultTableModel model = new DefaultTableModel();
        // Agregar columnas
        calificaciones.get(0).keySet().forEach(model::addColumn);
        // Agregar filas
        for (Map<String, Object> m : calificaciones) {
            model.addRow(m.values().toArray());
        }
        JOptionPane.showMessageDialog(this, "Mostrando registros.");
        table.setModel(model);
    }

    private void mostrarMaestrosFaltantesEnTabla() {
        if (listaCalificaciones == null || listaMaestros == null) return;

        // Obtener IDs de maestros con calificaciones
        var maestrosConCalif = listaCalificaciones.stream()
                .map(c -> String.valueOf(c.get("id_maestro")))
                .collect(java.util.stream.Collectors.toSet());

        // Filtrar maestros que no estén en esa lista
        var maestrosSinCalif = listaMaestros.stream()
                .filter(m -> !maestrosConCalif.contains(String.valueOf(m.get("id"))))
                .toList();

        if (maestrosSinCalif.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los maestros tienen calificaciones.");
            return;
        }

        // Mostrar en la tabla
        DefaultTableModel model = new DefaultTableModel();
        maestrosSinCalif.get(0).keySet().forEach(model::addColumn);
        for (Map<String, Object> m : maestrosSinCalif) {
            model.addRow(m.values().toArray());
        }

        JOptionPane.showMessageDialog(this, "Mostrando maestros sin calificaciones.");
        table.setModel(model);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppCalificaciones().setVisible(true));
    }
}
