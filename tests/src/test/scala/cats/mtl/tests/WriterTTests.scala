package cats
package mtl
package tests

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.instances.all._
import cats.laws.discipline.{ExhaustiveCheck, SerializableTests}
import cats.laws.discipline.eq._
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline._
import cats.mtl.lifting.{ApplicativeLayerFunctor, FunctorLayerFunctor, MonadLayerControl}
import org.scalacheck._

class WriterTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  implicit def eqKleisli[F[_], A, B](implicit arb: ExhaustiveCheck[A], ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] = {
    Eq.by((x: (Kleisli[F, A, B])) => x.run)
  }

  implicit def stateTEq[F[_], S, A](implicit S: ExhaustiveCheck[S], FSA: Eq[F[(S, A)]], F: FlatMap[F]): Eq[StateT[F, S, A]] = {
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state =>
      s => state.run(s))
  }

  type WriterTStringOverWriterTStringOverOption[A] = WriterT[WriterTC[Option, String]#l, List[Int], A]

  locally {
      import cats.laws.discipline.arbitrary._
      import cats.mtl.instances.all._

      checkAll("WriterT[WriterTC[Option, String]#l, List[Int], String]",
        ApplicativeCensorTests[WriterTStringOverWriterTStringOverOption, String]
          .applicativeCensor[String, String]
      )
      checkAll("ApplicativePass[WriterT[WriterTC[Option, String]#l, List[Int], String]",
        SerializableTests.serializable(ApplicativeCensor[WriterTStringOverWriterTStringOverOption, String]))

      checkAll("ReaderT[WriterTC[Option, String]#l, List[Int], String]",
        ApplicativeCensorTests[ReaderTStringOverWriterTStringOverOption, String]
          .applicativeCensor[String, String])
      checkAll("ApplicativePass[ReaderT[WriterTC[Option, String]#l, List[Int], String]",
        SerializableTests.serializable(ApplicativeCensor[ReaderTStringOverWriterTStringOverOption, String]))

      checkAll("StateT[WriterTC[Option, String]#l, List[Int], String]",
        ApplicativeCensorTests[StateTStringOverWriterTStringOverOption, String]
          .applicativeCensor[String, String])
      checkAll("ApplicativePass[StateT[WriterTC[Option, String]#l, List[Int], String]",
        SerializableTests.serializable(ApplicativeCensor[StateTStringOverWriterTStringOverOption, String]))
  }

  locally {
    import cats.laws.discipline.arbitrary._
    import cats.mtl.instances.censor._

    checkAll("WriterT[Option, String, String]",
      ApplicativeCensorTests[WriterTC[Option, String]#l, String]
        .applicativeCensor[String, String])
    checkAll("FunctorListen[WriterT[Option, String, ?]]",
      SerializableTests.serializable(FunctorListen[WriterTC[Option, String]#l, String]))

    {
      implicit val monadLayerControl: MonadLayerControl.Aux[WriterTC[Option, String]#l, Option, TupleC[String]#l] =
        cats.mtl.instances.writert.writerMonadLayerControl[Option, String]
      checkAll("WriterT[Option, String, ?]",
        MonadLayerControlTests[WriterTC[Option, String]#l, Option, TupleC[String]#l]
          .monadLayerControl[Boolean, Boolean])
      checkAll("MonadLayerControl[WriterT[Option, String, ?], Option]",
        SerializableTests.serializable(monadLayerControl))
    }

    {
      implicit val applicativeLayerFunctor: ApplicativeLayerFunctor[WriterTC[Option, String]#l, Option] =
        cats.mtl.instances.writert.writerApplicativeLayerFunctor[Option, String]
      checkAll("WriterT[Option, String, ?]",
        ApplicativeLayerFunctorTests[WriterTC[Option, String]#l, Option]
          .applicativeLayerFunctor[Boolean, Boolean])
      checkAll("ApplicativeLayerFunctor[WriterT[Option, String, ?], Option]",
        SerializableTests.serializable(applicativeLayerFunctor))
    }

    {
      implicit val functorLayerFunctor: FunctorLayerFunctor[WriterTC[Option, String]#l, Option] =
        cats.mtl.instances.writert.writerFunctorLayerFunctor[Option, String]
      checkAll("WriterT[Option, String, ?]",
        FunctorLayerFunctorTests[WriterTC[Option, String]#l, Option]
          .functorLayerFunctor[Boolean])
      checkAll("FunctorLayerFunctor[WriterT[Option, String, ?], Option]",
        SerializableTests.serializable(functorLayerFunctor))
    }
  }
}
