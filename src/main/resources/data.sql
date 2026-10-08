INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);

INSERT INTO Usuario(id, activo, email, password, rol) VALUES(2, true, 'admin@gmail.com', '1234', 'ADMIN');

INSERT INTO Usuario(id, activo, email, password, rol) VALUES(3, true, 'usuario@gmail.com', '1234', 'USUARIO');


INSERT INTO Torneo(cantidadAmaSusp, cantidadDeEquipos, cantidadMaxJugadores, cantidadMinJugadores, idaYVuelta, id, nombre)
Values(2, 2, 20, 11, null, 1,'copa');


INSERT INTO Equipo(id, codigo, colorLocal1, colorLocal2, colorVisitante1, colorVisitante2, escudo, nombre)
Values(1, 'CABJ', 'a', 'b', 'c', 'd', 'e', 'Boca');

INSERT INTO Equipo(id, codigo, colorLocal1, colorLocal2, colorVisitante1, colorVisitante2, escudo, nombre)
Values(2, 'CARP', 'a', 'b', 'c', 'd', 'e', 'River');

INSERT INTO Equipo(id, codigo, colorLocal1, colorLocal2, colorVisitante1, colorVisitante2, escudo, nombre)
Values(3, 'CAR', 'a', 'b', 'c', 'd', 'e', 'Racing');

INSERT INTO Equipo(id, codigo, colorLocal1, colorLocal2, colorVisitante1, colorVisitante2, escudo, nombre)
Values(4, 'CAI', 'a', 'b', 'c', 'd', 'e', 'Independiente');

INSERT INTO Equipo(id, codigo, colorLocal1, colorLocal2, colorVisitante1, colorVisitante2, escudo, nombre)
Values(5, 'CASL', 'a', 'b', 'c', 'd', 'e', 'San Lorenzo');

INSERT INTO Equipo(id, codigo, colorLocal1, colorLocal2, colorVisitante1, colorVisitante2, escudo, nombre)
Values(6, 'CELP', 'a', 'b', 'c', 'd', 'e', 'Estudiantes de La Plata');

INSERT INTO Equipo(id, codigo, colorLocal1, colorLocal2, colorVisitante1, colorVisitante2, escudo, nombre)
Values(7, 'CAU', 'a', 'b', 'c', 'd', 'e', 'Union');

INSERT INTO Equipo(id, codigo, colorLocal1, colorLocal2, colorVisitante1, colorVisitante2, escudo, nombre)
Values(8, 'CAH', 'a', 'b', 'c', 'd', 'e', 'Huracan');

INSERT INTO Equipo(id, codigo, colorLocal1, colorLocal2, colorVisitante1, colorVisitante2, escudo, nombre)
Values(9, 'CAVS', 'a', 'b', 'c', 'd', 'e', 'Velez');

INSERT INTO Equipo(id, codigo, colorLocal1, colorLocal2, colorVisitante1, colorVisitante2, escudo, nombre)
Values(10, 'CAB', 'a', 'b', 'c', 'd', 'e', 'Belgrano');


INSERT INTO EquipoTorneo(golesAFavor, golesEnContra, partidoPerdidos, partidosEmpatados, partidosGanados, partidosJugados, puntos, equipo_id, id, torneo_id)
Values(0, 0, 0, 0, 0, 0, 0, 1, 1, 1);

INSERT INTO EquipoTorneo(golesAFavor, golesEnContra, partidoPerdidos, partidosEmpatados, partidosGanados, partidosJugados, puntos, equipo_id, id, torneo_id)
Values(0, 0, 0, 0, 0, 0, 0, 2, 2, 1);

INSERT INTO EquipoTorneo(golesAFavor, golesEnContra, partidoPerdidos, partidosEmpatados, partidosGanados, partidosJugados, puntos, equipo_id, id, torneo_id)
Values(0, 0, 0, 0, 0, 0, 0, 3, 3, 1);

INSERT INTO EquipoTorneo(golesAFavor, golesEnContra, partidoPerdidos, partidosEmpatados, partidosGanados, partidosJugados, puntos, equipo_id, id, torneo_id)
Values(0, 0, 0, 0, 0, 0, 0, 4, 4, 1);

INSERT INTO EquipoTorneo(golesAFavor, golesEnContra, partidoPerdidos, partidosEmpatados, partidosGanados, partidosJugados, puntos, equipo_id, id, torneo_id)
Values(1, 1, 0, 0, 1, 1, 1, 5, 5, 1);

INSERT INTO EquipoTorneo(golesAFavor, golesEnContra, partidoPerdidos, partidosEmpatados, partidosGanados, partidosJugados, puntos, equipo_id, id, torneo_id)
Values(1, 1, 0, 0, 1, 1, 2, 6, 6, 1);

INSERT INTO EquipoTorneo(golesAFavor, golesEnContra, partidoPerdidos, partidosEmpatados, partidosGanados, partidosJugados, puntos, equipo_id, id, torneo_id)
Values(2, 0, 0, 0, 2, 2, 4, 7, 7, 1);

INSERT INTO EquipoTorneo(golesAFavor, golesEnContra, partidoPerdidos, partidosEmpatados, partidosGanados, partidosJugados, puntos, equipo_id, id, torneo_id)
Values(2, 0, 0, 0, 2, 2, 5, 8, 8, 1);

INSERT INTO EquipoTorneo(golesAFavor, golesEnContra, partidoPerdidos, partidosEmpatados, partidosGanados, partidosJugados, puntos, equipo_id, id, torneo_id)
Values(2, 0, 0, 0, 2, 2, 2, 9, 9, 1);
INSERT INTO EquipoTorneo(golesAFavor, golesEnContra, partidoPerdidos, partidosEmpatados, partidosGanados, partidosJugados, puntos, equipo_id, id, torneo_id)
Values(0, 2, 2, 0, 0, 2, 0, 10, 10, 1);

delete from EquipoTorneo;

delete from Equipo;

delete from Torneo;