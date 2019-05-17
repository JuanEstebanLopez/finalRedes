package servidor.modelo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import comun.Jugador;

public class BaseDatos implements Serializable {
	public final static String BD = "bd/datos.txt";
	public final static String REGISTRO = "bd/registros.txt";
	private HashMap<String, UsuarioRegistrado> datos;
	private LinkedList<RegistroPartida> registrosPartidas;

	public BaseDatos() {
		leer();
	}

	/**
	 * Agrega un nuevo usuario.
	 * 
	 * @param usuario
	 * @param password
	 * @param email
	 * @return true si el usuario se pudo registrar, false si el usuario existe o no
	 *         se pudo guardar el archivo.
	 */
	public boolean registrarUsuario(String usuario, String password, String email) {
		if (verificarUsuarioExistente(email)) {
			return false;
		} else {
			UsuarioRegistrado registrado = new UsuarioRegistrado(usuario, email, password);
			datos.put(email, registrado);
			return guardar();
		}
	}

	/**
	 * Verifica si un usuario existe.<br>
	 * Se usa para saber si un usuario se puede registrar.
	 * 
	 * @param email
	 * @return true si el usuario ya existe, false en caso contrario.
	 */
	public boolean verificarUsuarioExistente(String email) {
		return datos.containsKey(email);
	}

	/**
	 * Verifica si el usuario tiene su contraseña correcta.<br>
	 * Se usa para verificar si un usuario puede iniciar sesión.
	 * 
	 * @param email
	 * @param pass
	 * @return true si el usuario es válido, false en caso contrario.
	 */
	public boolean verificarInicioSesion(String email, String pass) {
		return datos.containsKey(email) && datos.get(email).getPassword().equals(pass);
	}

	@SuppressWarnings("unchecked")
	public void leer() {
		try {
			ObjectInputStream br = new ObjectInputStream(new FileInputStream(BD));
			datos = (HashMap<String, UsuarioRegistrado>) br.readObject();
			br.close();
		} catch (FileNotFoundException e) {
			datos = new HashMap<String, UsuarioRegistrado>();
			guardar();
			leer();
			e.printStackTrace();
		} catch (IOException s) {
			s.printStackTrace();
		} catch (ClassNotFoundException m) {
			m.printStackTrace();
		}
		System.out.println(datos);
	}

	public boolean guardar() {
		try {
			System.out.println(datos);
			ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(BD, false));
			salida.writeObject(datos);
			salida.flush();
			salida.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private class UsuarioRegistrado implements Serializable {

		private String nombre;
		private String email;
		private String password;

		public UsuarioRegistrado(String nombre, String email, String password) {
			this.nombre = nombre;
			this.email = email;
			this.password = password;
		}

		public String getNombre() {
			return nombre;
		}

		public void setNombre(String nombre) {
			this.nombre = nombre;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

	}

	public LinkedList<RegistroPartida> getRegistros(String user) {
		LinkedList<RegistroPartida> registros = new LinkedList<RegistroPartida>();
		leerRegistros();
		Iterator<RegistroPartida> it = registrosPartidas.iterator();
		while (it.hasNext()) {
			RegistroPartida p = it.next();
			if (p.contieneJugador(user))
				registros.add(p);
		}
		return registros;
	}

	public void leerRegistros() {
		registrosPartidas = REGISTRO_PARTIDAS();
		if (registrosPartidas == null) {
			registrosPartidas = new LinkedList<RegistroPartida>();
			guardarRegistro();
			leerRegistros();
		}

	}

	@SuppressWarnings("unchecked")
	public static LinkedList<RegistroPartida> REGISTRO_PARTIDAS() {
		LinkedList<RegistroPartida> registrosPartidas = new LinkedList<RegistroPartida>();
		try {
			ObjectInputStream br = new ObjectInputStream(new FileInputStream(REGISTRO));
			registrosPartidas = (LinkedList<RegistroPartida>) br.readObject();
			br.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException s) {
			s.printStackTrace();
		} catch (ClassNotFoundException m) {
			m.printStackTrace();
		}
		System.out.println(registrosPartidas);
		return registrosPartidas;
	}

	public boolean guardarRegistro() {
		try {
			System.out.println(registrosPartidas);
			ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(REGISTRO, false));
			salida.writeObject(registrosPartidas);
			salida.flush();
			salida.close();
			if (registrosPartidas.size() > 0) {
				RegistroPartida r = registrosPartidas.get(registrosPartidas.size() - 1);

				System.out.println(
						"GUARDADOS:" + r.jugadores.size() + "  " + Arrays.toString(r.jugadores.keySet().toArray()));
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void registrarPartida(String fecha, long tiempoTranscurrido, Jugador[] jugadores) {
		leerRegistros();
		RegistroPartida reg = new RegistroPartida(fecha, tiempoTranscurrido, jugadores);
		registrosPartidas.add(reg);
		guardarRegistro();

	}

	public class RegistroPartida implements Serializable {
		private HashMap<String, RegistroJugador> jugadores;
		private String fecha;
		private String tiempoTranscurrido;

		public RegistroPartida(String fecha, long tiempo, Jugador[] jugador) {

			this.jugadores = new HashMap<String, RegistroJugador>(5);
			System.out.println("Reg Part " + Arrays.toString(jugador));
			for (int i = 0; i < jugador.length; i++) {
				Jugador j = jugador[i];
				RegistroJugador juga = new RegistroJugador(j.getAlimentosConsumidos(), j.getPuntaje(),
						j.getPuntajeTotal(), j.isGanador());
				jugadores.put(j.getNombre(), juga);
			}

			this.fecha = fecha;

			long ms = TimeUnit.MILLISECONDS.toSeconds(tiempo);
			long m = ms / 60;
			long s = ms % 50;
			String t = m + ":" + s;
			this.tiempoTranscurrido = t;
		}

		public RegistroJugador jugador(String name) {
			return jugadores.get(name);
		}

		public boolean contieneJugador(String juga) {
			return jugadores.keySet().contains(juga);
		}

		public HashMap<String, RegistroJugador> getJugadores() {
			return jugadores;
		}

		public void setJugadores(HashMap<String, RegistroJugador> jugadores) {
			this.jugadores = jugadores;
		}

		public String getFecha() {
			return fecha;
		}

		public void setFecha(String fecha) {
			this.fecha = fecha;
		}

		public String getTiempoTranscurrido() {
			return tiempoTranscurrido;
		}

		public void setTiempoTranscurrido(String tiempoTranscurrido) {
			this.tiempoTranscurrido = tiempoTranscurrido;
		}

	}

	public class RegistroJugador implements Serializable {
		private int alimentosDigeridos;
		private int score;
		private int positiveScore;
		private boolean gano;

		public RegistroJugador(int alimentosDigeridos, int score, int positiveScore, boolean gano) {
			this.alimentosDigeridos = alimentosDigeridos;
			this.score = score;
			this.positiveScore = positiveScore;
			this.gano = gano;
		}

		public int getAlimentosDigeridos() {
			return alimentosDigeridos;
		}

		public void setAlimentosDigeridos(int alimentosDigeridos) {
			this.alimentosDigeridos = alimentosDigeridos;
		}

		public int getScore() {
			return score;
		}

		public void setScore(int score) {
			this.score = score;
		}

		public int getPositiveScore() {
			return positiveScore;
		}

		public void setPositiveScore(int positiveScore) {
			this.positiveScore = positiveScore;
		}

		public boolean isGano() {
			return gano;
		}

		public void setGano(boolean gano) {
			this.gano = gano;
		}

	}

}
