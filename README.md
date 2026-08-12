<p align="center"><img src="docs/banner.svg" alt="SlimefunAdvancements" width="100%"></p>

# SlimefunAdvancements

Un sistema de progresos propio de Slimefun, adaptado al ecosistema de **DrakesCraft**
(Paper/Purpur 1.21.11, Java 21).

## Qué hace

Añade progresos como los de Minecraft pero para Slimefun: fabricar un objeto, investigar algo,
buscar un término en la guía, matar una criatura. Se organizan en grupos, se ven en una interfaz
propia con su árbol, y se anuncian al conseguirlos.

Los progresos se definen en YAML (`sfadvancements.yml` y `sfagroups.yml`), así que se pueden
escribir sin tocar código. Además puede **importar** los que traiga otro addon en su jar, con lo
que cada expansión puede aportar los suyos.

Comandos: `/sfa grant`, `/sfa revoke`, `/sfa import`, `/sfa reload`.

## Qué cambiamos

Este repositorio **no es un fork**: es el código original integrado en el ecosistema de
DrakesCraft.

**Se autodesactivaba.** El fork chino le puso un candado al arrancar: si no encontraba
`GuizhanLibPlugin`, el plugin se apagaba solo y escribía en consola que hacía falta descargarla.
No usamos esa librería —arrastra un autoactualizador que reemplaza el jar—, así que el candado
está fuera.

**Y usaba sus clases, no solo el candado.** Esto costó una lección: quitar el candado no bastaba.
`Utils.makeShiny()` seguía llamando a `EnchantmentX` de GuizhanLib, y sin la librería lanzaba
`NoClassDefFoundError` **en cada clic de inventario**. Ahora usa
`setEnchantmentGlintOverride(true)`, nativo desde 1.20.5 y además mejor: el original metía un
encantamiento de verdad y lo escondía con un ItemFlag, con lo que se colaba en yunques y mesas de
encantar.

**Fuera la integración con JustEnoughGuide.** El fork traía una rama para el historial de guía de
JEG, que no usamos. Su propio código ya contemplaba que faltase, así que se quedó el camino de
siempre.

**Fuera el autoactualizador.** Aquí la clase se llamaba `GuizhanUpdater`, no
`GuizhanBuildsUpdater` como en los demás addons: por eso se escapó en la primera pasada.

**Todo en español**, interfaz, comandos y consola.

## Instalación

Necesita Slimefun de DrakesCraft (`Slimefun4-Drake`). Se pone el jar en `plugins/` y listo.

## Crédito

El trabajo de fondo es de **char321**. Licencia **GPL-3.0**, conservada sin modificar. Los
detalles están en [UPSTREAM.md](UPSTREAM.md).
