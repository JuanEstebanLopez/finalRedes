package servidor.modelo;

import java.awt.image.BufferedImage;
import java.beans.Encoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import servidor.modelo.BaseDatos.RegistroJugador;
import servidor.modelo.BaseDatos.RegistroPartida;

public class WebCllientHandler implements Runnable {

	private final Socket socket;
	private BaseDatos bd;

	public WebCllientHandler(Socket socket, BaseDatos bd) {
		this.socket = socket;
		this.bd = bd;
	}

	@Override
	public void run() {
		System.out.println("\nClientHandler Started for " + this.socket);
		handleRequest(this.socket);
	}

	public void handleRequest(Socket socket) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String headerLine = in.readLine();
			// A tokenizer is a process that splits text into a series of tokens
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			// The nextToken method will return the next available token
			String httpMethod = tokenizer.nextToken();
			// The next code sequence handles the GET method. A message is displayed on the
			// server side to indicate that a GET method is being processed
			if (httpMethod.equals("GET")) {
				System.out.println("Get method processed");
				String httpQueryString = tokenizer.nextToken();
				// System.out.println("\n\n\n\n OBTENIDO:\n");
				System.out.println(httpQueryString);
				// System.out.println("\n\n\n\n");

				HashMap<String, String> files = new HashMap<String, String>();
				files.put("/", "data/web/index.html");
				files.put("/css", "data/web/css/style.css");

				if (files.keySet().contains(httpQueryString)) {
					StringBuilder responseBuffer = new StringBuilder();
					String str = "";
					String file = files.get(httpQueryString);
					BufferedReader buf = new BufferedReader(new FileReader(file));
					while ((str = buf.readLine()) != null) {
						responseBuffer.append(str);
					}
					String mime = "text/html";
					if (file.endsWith(".css"))
						mime = "text/css";
					else if (file.endsWith(".js"))
						mime = "text/javascript";
					else if (file.endsWith(".json"))
						mime = "application/json";

					sendResponse(socket, 200, responseBuffer.toString(), mime);
					buf.close();
				} else if (httpQueryString.contains("?")) {
					String name = "";
					String pass = "";

					String routeParams = httpQueryString.split("\\?")[1];
					System.out.println(routeParams);
					if (!"".equals(routeParams)) {
						String[] params = { routeParams };
						if (routeParams.contains("&"))
							params = routeParams.split("&");
						System.out.println(params.length);
						for (int i = 0; i < params.length; i++) {
							String p = params[i];
							if (p.startsWith("name="))
								name = p.split("=")[1];
							else if (p.startsWith("password="))
								pass = p.split("=")[1];
						}
					}
					System.out.println("name: " + name + ",  Pass: " + pass);
					if (!name.equals("") && !pass.equals("") && bd.verificarInicioSesion(name, pass)) {
						LinkedList<RegistroPartida> registros = bd.getRegistros(name);
						StringBuilder responseBuffer = new StringBuilder();
						responseBuffer.append("<html>")
								.append("<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"css\">  </head>")
								.append("<body>").append("<h1>Registro de partidas para el jugador " + name + " ("
										+ registros.size() + " partidas)</h1><br>");

						responseBuffer.append(
								"<table><tr><th>Fecha</th><th>Alimentos Digeridos</th><th>Final Score</th><th>Positive Score</th><th>Ganó</th><th>Tiempo</th></tr>");

						Iterator<RegistroPartida> it = registros.iterator();
						while (it.hasNext()) {
							RegistroPartida p = it.next();
							RegistroJugador j = p.jugador(name);
							System.out.println("NOMBRESSS : \n" + Arrays.toString(p.getJugadores().keySet().toArray())
									+ " " + p.getJugadores().size());

							responseBuffer.append("<tr><td>").append(p.getFecha()).append("</td><td>")
									.append(j.getAlimentosDigeridos()).append("</td><td>").append(j.getScore())
									.append("</td><td>").append(j.getPositiveScore()).append("</td><td>")
									.append(j.isGano()?"Sí":"No").append("</td><td>").append(p.getTiempoTranscurrido())
									.append("</td></tr>");
						}
						responseBuffer.append("</table><br><a href=\"/\">Volver</a>").append("</body>")
								.append("</html>");
						sendResponse(socket, 200, responseBuffer.toString(), "text/html");

					} else {
						StringBuilder responseBuffer = new StringBuilder();
						responseBuffer.append("<html>")
								.append("<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"css\">  </head>")
								.append("<body>").append("<h1>Error en los datos ingresados</h1><br>")
								.append("<a href=\"/\">Volver</a>").append("</body>").append("</html>");
						sendResponse(socket, 200, responseBuffer.toString(), "text/html");
					}

				} else if (httpQueryString.equals("/bkg")) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					BufferedImage bf = ImageIO.read(new File("data/img/bkg.png"));
					ImageIO.write(bf, "png", bos);
					byte[] imageBytes = bos.toByteArray();
					
					String imageString = Base64.getEncoder().encodeToString(imageBytes);
					bos.close();
					System.out.println(imageString);
					String mime = "image/png";
					sendResponse(socket, 200, imageString, mime);

				} else {
					StringBuilder responseBuffer = new StringBuilder();
					responseBuffer.append("<html>").append("<body>")
							.append("<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"css\">  </head><br>")
							.append("<font color='white'>[0][1][0]</font><br>")
							.append("<font color='white'>[0][0][1]</font><br>")
							.append("<font color='white'>[1][1][1]</font><br>")
							.append("<center><font color='white'<h1> HACK ME KUKY! </h1> </font></center>")
							.append("<center><img src='https://s2.glbimg.com/QJD0YP7szRqJuSEUdGHPF_2Dwqs=/850x446/s.glbimg.com/po/tt/f/original/2012/06/01/pirata-e1314380534977.jpg'></center>")
							.append("</body>").append("</html>");
					sendResponse(socket, 200, responseBuffer.toString(), "text/html");
				}
			} else {
				System.out.println("The HTTP method is not recognized");
				sendResponse(socket, 405, "Method Not Allowed", "text/html");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendResponse(Socket socket, int statusCode, String responseString, String mime) {

		String statusLine;
		String serverHeader = "Server: WebServer\r\n";
		String contentTypeHeader = "Content-Type: " + mime + "\r\n";

		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			if (statusCode == 200) {
				statusLine = "HTTP/1.0 200 OK" + "\r\n";
				String contentLengthHeader = "Content-Length: " + responseString.length() + "\r\n";
				out.writeBytes(statusLine);
				out.writeBytes(serverHeader);
				out.writeBytes(contentTypeHeader);
				out.writeBytes(contentLengthHeader);
				out.writeBytes("\r\n");
				out.writeBytes(responseString);
			} else if (statusCode == 405) {
				statusLine = "HTTP/1.0 405 Method Not Allowed" + "\r\n";
				out.writeBytes(statusLine);
				out.writeBytes("\r\n");
			} else {
				statusLine = "HTTP/1.0 404 Not Found" + "\r\n";
				out.writeBytes(statusLine);
				out.writeBytes("\r\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
