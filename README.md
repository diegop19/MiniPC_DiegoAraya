# MiniPC
# Diego Araya Ureña - 2023071310

Estado del proyecto:  [4]
Enlace del video: https://youtu.be/4qHD9bfHg74
Mini PC - Simulador de Instrucciones

Este proyecto es una aplicación de escritorio desarrollada en Java con Swing que simula el funcionamiento de un mini computador. La aplicación permite cargar un archivo de texto con extensión .asm, interpretar sus instrucciones, traducirlas a su representación binaria y ejecutarlas sobre un CPU simulado, ya sea instrucción por instrucción o de forma completa. Todo el proceso se puede observar en tiempo real a través de una interfaz gráfica, que muestra tanto el contenido de la memoria como el estado de los registros del procesador en cada momento de la ejecución.

El set de instrucciones soportado es MOV, LOAD, STORE, ADD y SUB, que trabajan sobre cuatro registros de propósito general AX, BX, CX, DX y un registro acumulador (AC) utilizado internamente por el CPU para realizar las operaciones aritméticas. Cada instrucción se traduce a una palabra binaria de 16 bits, compuesta por 4 bits de opcode, 4 bits de registro, 1 bit de signo y 7 bits de magnitud para el valor numérico.

El proyecto sigue el patrón de diseño Modelo-Vista-Controlador (MVC)

Estructura de clases

El paquete Modelo concentra toda la lógica interna del simulador. La clase Instruccion es un objeto simple que almacena una línea del programa ya procesada, junto con su opcode, registro, valor numérico y su traducción binaria. La clase Ensamblador se encarga de leer el archivo .asm línea por línea, validar que cada una cumpla con el formato esperado (operador reconocido, registro válido y valor dentro del rango permitido) y construir los objetos Instruccion correspondientes; si encuentra una línea inválida, lanza la excepción personalizada FormatoInvalidoException con un mensaje descriptivo. La clase Memoria representa el espacio de almacenamiento del Mini PC, dividido en dos zonas fijas: las primeras 64 posiciones reservadas para el sistema operativo y el resto disponible para el programa del usuario; su tamaño total es configurable desde la interfaz, respetando siempre un mínimo de 128 posiciones. La clase CPU contiene los registros del procesador y el ciclo de ejecución fetch-decode-execute, permitiendo avanzar una instrucción a la vez o ejecutar el programa completo. Finalmente, la clase BCP representa el Bloque de Control de Proceso, reflejando en todo momento el estado del CPU (registros, PC, instrucción actual) junto con información adicional del proceso, como su identificador, estado (nuevo, ejecutando, terminado) y el rango de memoria asignado.

El paquete Vista contiene la clase MainFrame, que construye toda la interfaz gráfica: los botones de control, las tablas de instrucciones y memoria, y el panel que muestra el estado actual del BCP y los registros del CPU.

El paquete Controlador contiene la clase Controlador, que conecta la vista con el modelo. Se encarga de responder a las acciones del usuario (cargar archivo, ejecutar, ejecutar paso a paso, limpiar, configurar memoria y consultar estadísticas), delegando la lógica correspondiente a las clases del modelo y refrescando la interfaz con los resultados obtenidos.



El tamaño de memoria solo puede modificarse antes de cargar un programa o después de utilizar la opción de limpiar, ya que cambiarlo en medio de una ejecución invalidaría el estado actual del CPU y del programa cargado. El programa también valida que el archivo seleccionado tenga extensión .asm antes de intentar procesarlo, y detiene el parseo en la primera línea que no cumpla con el formato esperado, mostrando un mensaje de error específico al usuario.
