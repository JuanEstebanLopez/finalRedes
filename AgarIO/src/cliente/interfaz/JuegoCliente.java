package cliente.interfaz;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import cliente.modelo.Comunicacion;
import comun.Elemento;
import comun.PanelJuego;

public class JuegoCliente extends PanelJuego {

	/**
	 * �ndice del jugador en la lista de jugadores.
	 */
	private int jugadorIndex;
	private int mx, my;
	private InterfazCliente principal;

	private String[] jugadores;
	private String comida;
	private int playerX;
	private int playerY;

	public JuegoCliente(InterfazCliente principal) {
		super();
		this.jugadores = new String[0];
		this.comida = "";
		this.principal = principal;
		jugadorIndex = -1;
		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				mx = e.getX() - WIDTH / 2;
				my = e.getY() - HEIGHT / 2;
				// System.out.println(mx + ", " + my);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});

	}

	public ArrayList<Color> colorear() {
		ArrayList<Color> colores = new ArrayList<Color>();
		colores.add(Color.BLUE);
		colores.add(Color.RED);
		colores.add(Color.CYAN);
		colores.add(Color.YELLOW);
		colores.add(Color.MAGENTA);
		colores.add(Color.ORANGE);
		colores.add(Color.PINK);
		colores.add(Color.GREEN);
		colores.add(Color.WHITE);
		return colores;
	}

	@Override
	public void pintarComida(Graphics g) {
		if (!"".equals(comida) && !"".equals(comida.trim())) {
			String[] cs = comida.split(Comunicacion.SEPARADOR);
			for (int i = 0; i < cs.length; i++) {
				String[] cInfo = cs[i].split(Comunicacion.SEPARADOR_MIN);
				int color = Integer.parseInt(cInfo[0]);
				int px = Integer.parseInt(cInfo[1]) + playerX;
				int py = Integer.parseInt(cInfo[2]) + playerY;
				g.setColor(colorear().get(color));
				g.fillOval(px, py, Elemento.COMIDA_DIAMETER, Elemento.COMIDA_DIAMETER);

			}
		}
	}

	@Override
	public void pintarJugadores(Graphics g) {
		if (jugadores.length >= 0) {
			for (int i = 0; i < jugadores.length; i++) {
				String[] jInfo = jugadores[i].split(Comunicacion.SEPARADOR_MIN);
				int color = Integer.parseInt(jInfo[0]);
				int px = Integer.parseInt(jInfo[1]) + playerX;
				int py = Integer.parseInt(jInfo[2]) + playerY;
				int d = Integer.parseInt(jInfo[3]);
				g.setColor(colorear().get(color));
				g.fillOval(px, py, d, d);
			}
		}
	}

	@Override
	public void update() {
		if (jugadorIndex >= 0)
			principal.actualizarPosiciones(mx, my);
	}

	public void setJugadorIndex(int jugadorIndex) {
		this.jugadorIndex = jugadorIndex;
	}

	public int getJugadorIndex() {
		return jugadorIndex;
	}

	/**
	 * Actualiza los elementos del juego obtenidos del servidor
	 * 
	 * @param juga
	 *            informaci�n de los jugadores
	 * @param comida
	 *            Informaci�n de la comida
	 */
	public void actualizarElementosJuego(String juga, String comida) {
		this.jugadores = juga.split(Comunicacion.SEPARADOR);
		this.comida = comida;
		if (jugadorIndex >= 0) {
			String[] j = jugadores[jugadorIndex].split(Comunicacion.SEPARADOR_MIN);
			int d = Integer.parseInt(j[3]);
			playerX = WIDTH / 2 - Integer.parseInt(j[1]) - d / 2;
			playerY = HEIGHT / 2 - Integer.parseInt(j[2]) - d / 2;
		} else {
			playerX = 0;
			playerY = 0;
		}
	}

}
