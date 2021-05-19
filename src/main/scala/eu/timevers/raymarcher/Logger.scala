package eu.timevers.raymarcher

import cats.Id
import cats.effect.Sync
import monix.eval.Task

trait Logger[F[_]]:
  def info(s: String): F[Unit]

given [F[_]: Sync]: Logger[F] with
  def info(s: String): F[Unit] =
    Sync[F].delay(System.err.nn.println(s"[INFO] $s"))

given Logger[Id] with
  def info(s: String): Id[Unit] = ()
