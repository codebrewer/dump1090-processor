/*
 * Copyright 2019, 2020 Mark Scott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codebrewer.dump1090processor.basestation.entity;

import com.openpojo.random.RandomFactory;
import com.openpojo.random.RandomGenerator;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoField;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.rule.Rule;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EntityTest {
  private static class PointRandomGenerator implements RandomGenerator {
    private static final Geographic2DCoordinateReferenceSystem COORDINATE_REFERENCE_SYSTEM =
        CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);
    private static final Collection<Class<?>> TYPES = Collections.singletonList(Point.class);
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public Object doGenerate(Class<?> type) {
      return new Point<>(
          new G2D(RANDOM.nextDouble(), RANDOM.nextDouble()), COORDINATE_REFERENCE_SYSTEM);
    }

    public Collection<Class<?>> getTypes() {
      return TYPES;
    }
  }

  private static class SetterMustNotExistRule implements Rule {
    public void evaluate(final PojoClass pojoClass) {
      for (PojoField fieldEntry : pojoClass.getPojoFields()) {
        if (!fieldEntry.isFinal() && fieldEntry.hasSetter()) {
          Affirm.fail(String.format("[%s] has a setter", fieldEntry));
        }
      }
    }
  }

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    RandomFactory.addRandomGenerator(new PointRandomGenerator());
    validator = ValidatorBuilder.create()
                                .with(new GetterMustExistRule())
                                .with(new GetterTester())
                                .with(new SetterMustNotExistRule())
                                .build();
  }

  @Test
  void testBaseStationMessagePojoStructureAndBehavior() {
    validator.validate(PojoClassFactory.getPojoClass(BaseStationMessage.class));
  }

  @Test
  void testCallSignMessagePojoStructureAndBehavior() {
    validator.validate(PojoClassFactory.getPojoClass(CallSignMessage.class));
  }

  @Test
  void testIdMessagePojoStructureAndBehavior() {
    validator.validate(PojoClassFactory.getPojoClass(IdMessage.class));
  }

  @Test
  void testNewAircraftMessagePojoStructureAndBehavior() {
    validator.validate(PojoClassFactory.getPojoClass(NewAircraftMessage.class));
  }

  @Test
  void testStatusMessagePojoStructureAndBehavior() {
    validator.validate(PojoClassFactory.getPojoClass(StatusMessage.class));
  }

  @Test
  void testTransmissionMessagePojoStructureAndBehavior() {
    validator.validate(PojoClassFactory.getPojoClass(TransmissionMessage.class));
  }
}
