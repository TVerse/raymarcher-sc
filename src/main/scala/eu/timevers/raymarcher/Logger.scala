package eu.timevers.raymarcher

import cats.Id
import cats.effect.Sync
import cats.effect.std.Console

trait Logger[F[_]]:
  def info(s: String): F[Unit]

object Logger:
  def apply[F[_]](using L: Logger[F]): L.type = L

  given [F[_]: Console]: Logger[F] with
    def info(s: String): F[Unit] =
      Console[F].println(s"[INFO] $s")

  given Logger[Id] with
    def info(s: String): Id[Unit] = ()
