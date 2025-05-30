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

public class AppCalifaciones extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;
    private JTable table;
    private String jwtToken = "";

    public AppCalifaciones() {
        setTitle("Login y Maestros");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de login
        JPanel loginPanel = new JPanel(new FlowLayout());
        userField = new JTextField(10);
        passField = new JPasswordField(10);
        loginButton = new JButton("Iniciar sesión");
        loginPanel.add(new JLabel("Usuario:"));
        loginPanel.add(userField);
        loginPanel.add(new JLabel("Contraseña:"));
        loginPanel.add(passField);
        loginPanel.add(loginButton);
        add(loginPanel, BorderLayout.NORTH);

        // Tabla
        table = new JTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Acción de login
        loginButton.addActionListener(e -> hacerLogin());
    }

    private void hacerLogin() {
        String usuario = userField.getText();
        String contrasena = new String(passField.getPassword());

        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = String.format("{\"usuario\":\"%s\",\"contrasena\":\"%s\"}", usuario, contrasena);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/login"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                jwtToken = response.body(); // Aquí asumo que el body ES el token directamente
                obtenerMaestros();
            } else {
                JOptionPane.showMessageDialog(this, "Error de login: " + response.statusCode());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al hacer login");
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
                List<Map<String, Object>> maestros = mapper.readValue(
                        response.body(), new TypeReference<List<Map<String, Object>>>() {}
                );

                mostrarMaestrosEnTabla(maestros);
            } else {
                JOptionPane.showMessageDialog(this, "Error al obtener maestros: " + response.statusCode());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al obtener maestros");
        }
    }

    private void mostrarMaestrosEnTabla(List<Map<String, Object>> maestros) {
        if (maestros.isEmpty()) return;

        DefaultTableModel model = new DefaultTableModel();
        // Agregar columnas
        maestros.get(0).keySet().forEach(model::addColumn);
        // Agregar filas
        for (Map<String, Object> m : maestros) {
            model.addRow(m.values().toArray());
        }
        table.setModel(model);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppMaestros().setVisible(true));
    }
}
