package eu.timevers.raymarcher

import cats.Id
import cats.effect.Sync
import cats.effect.std.Console
import cats.Show
import cats.implicits.given

trait Logger[F[_]]:
  def info[S: Show](s: S): F[Unit]

object Logger:
  def apply[F[_]](using L: Logger[F]): L.type = L

  given [F[_]: Console]: Logger[F] with
    def info[S: Show](s: S): F[Unit] =
      Console[F].println(s"[INFO] ${s.show}")

  given Logger[Id] with
    def info[S: Show](s: S): Id[Unit] = ()
