INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);

INSERT INTO Usuario(id, activo, email, password, rol) VALUES(2, true, 'admin@gmail.com', '1234', 'ADMIN');

INSERT INTO Usuario(id, activo, email, password, rol) VALUES(3, true, 'usuario@gmail.com', '1234', 'USUARIO');

-- Torneo de demostracion para la vista de estadisticas
INSERT INTO Torneo(id, nombre, cantidadDeEquipos, cantidadMaxJugadores, cantidadMinJugadores, cantidadAmaSusp, idaYVuelta) VALUES(1, 'Torneo Demo Estadisticas', 4, 15, 11, 3, true);

-- Equipos participantes
INSERT INTO Equipo(id, nombre, escudo, ColorLocal, ColorVisitanate) VALUES(1, 'Los Leones', 'leones.png', '#ff0000', '#ffffff');
INSERT INTO Equipo(id, nombre, escudo, ColorLocal, ColorVisitanate) VALUES(2, 'Tigres FC', 'tigres.png', '#ff9900', '#000000');
INSERT INTO Equipo(id, nombre, escudo, ColorLocal, ColorVisitanate) VALUES(3, 'Aguilas del Sur', 'aguilas.png', '#0000ff', '#ffff00');
INSERT INTO Equipo(id, nombre, escudo, ColorLocal, ColorVisitanate) VALUES(4, 'Condores Unidos', 'condores.png', '#00ff00', '#990099');

-- Inscripcion en el torneo
INSERT INTO Torneo_Equipo(Torneo_id, equipos_id) VALUES(1, 1);
INSERT INTO Torneo_Equipo(Torneo_id, equipos_id) VALUES(1, 2);
INSERT INTO Torneo_Equipo(Torneo_id, equipos_id) VALUES(1, 3);
INSERT INTO Torneo_Equipo(Torneo_id, equipos_id) VALUES(1, 4);

-- Tabla de posiciones: cada metrica produce un orden distinto
-- puntos: Leones(9) Tigres(7) Aguilas(6) Condores(1)
-- goles a favor: Tigres(15) Leones(12) Aguilas(5) Condores(2)
-- partidos ganados: Leones(3) Tigres(2) Aguilas(2) Condores(0)
-- diferencia de gol: Leones(+8) Tigres(+6) Aguilas(+2) Condores(-16)
INSERT INTO EquipoTorneo(id, equipo_id, torneo_id, puntos, partidosJugados, partidosGanados, partidosEmpatados, partidoPerdidos, golesAFavor, golesEnContra) VALUES(1, 1, 1, 9, 4, 3, 0, 1, 12, 4);
INSERT INTO EquipoTorneo(id, equipo_id, torneo_id, puntos, partidosJugados, partidosGanados, partidosEmpatados, partidoPerdidos, golesAFavor, golesEnContra) VALUES(2, 2, 1, 7, 4, 2, 1, 1, 15, 9);
INSERT INTO EquipoTorneo(id, equipo_id, torneo_id, puntos, partidosJugados, partidosGanados, partidosEmpatados, partidoPerdidos, golesAFavor, golesEnContra) VALUES(3, 3, 1, 6, 4, 2, 0, 2, 5, 3);
INSERT INTO EquipoTorneo(id, equipo_id, torneo_id, puntos, partidosJugados, partidosGanados, partidosEmpatados, partidoPerdidos, golesAFavor, golesEnContra) VALUES(4, 4, 1, 1, 4, 0, 1, 3, 2, 18);

