/*
 * SonarQube
 * Copyright (C) 2009-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.ce.task;

import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@RunWith(DataProviderRunner.class)
public class CeTaskTest {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private CeTask.Builder underTest = new CeTask.Builder();

  @Test
  public void build_fails_with_NPE_if_organizationUuid_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("organizationUuid can't be null nor empty");

    underTest.build();
  }

  @Test
  public void build_fails_with_NPE_if_organizationUuid_is_empty() {
    underTest.setOrganizationUuid("");

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("organizationUuid can't be null nor empty");

    underTest.build();
  }

  @Test
  public void build_fails_with_NPE_if_uid_is_null() {
    underTest.setOrganizationUuid("org1");

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("uuid can't be null nor empty");

    underTest.build();
  }

  @Test
  public void build_fails_with_NPE_if_uuid_is_empty() {
    underTest.setOrganizationUuid("org1").setUuid("");

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("uuid can't be null nor empty");

    underTest.build();
  }

  @Test
  public void build_fails_with_NPE_if_type_is_null() {
    underTest.setOrganizationUuid("org1").setUuid("uuid");

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("type can't be null nor empty");

    underTest.build();
  }

  @Test
  public void build_fails_with_NPE_if_type_is_empty() {
    underTest.setOrganizationUuid("org1").setUuid("uuid").setType("");

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("type can't be null nor empty");

    underTest.build();
  }

  @Test
  @UseDataProvider("oneAndOnlyOneOfComponentAndMainComponent")
  public void build_fails_with_IAE_if_only_one_of_component_and_main_component_is_non_null(CeTask.Component component, CeTask.Component mainComponent) {
    underTest.setOrganizationUuid("org1");
    underTest.setType("TYPE_1");
    underTest.setUuid("UUID_1");
    underTest.setComponent(component);
    underTest.setMainComponent(mainComponent);

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("None or both component and main component must be non null");
    
    underTest.build();
  }

  @DataProvider
  public static Object[][] oneAndOnlyOneOfComponentAndMainComponent() {
    CeTask.Component component = new CeTask.Component("COMPONENT_UUID_1", "COMPONENT_KEY_1", "The component");
    return new Object[][] {
      {component, null},
      {null, component}
    };
  }

  @Test
  public void verify_getters() {
    CeTask.Component component = new CeTask.Component("COMPONENT_UUID_1", "COMPONENT_KEY_1", "The component");
    CeTask.Component mainComponent = new CeTask.Component("MAIN_COMPONENT_UUID_1", "MAIN_COMPONENT_KEY_1", "The main component");
    underTest.setOrganizationUuid("org1");
    underTest.setType("TYPE_1");
    underTest.setUuid("UUID_1");
    underTest.setSubmitterUuid("LOGIN_1");
    underTest.setComponent(component);
    underTest.setMainComponent(mainComponent);
    underTest.setCharacteristics(ImmutableMap.of("k1", "v1", "k2", "v2"));

    CeTask task = underTest.build();

    assertThat(task.getOrganizationUuid()).isEqualTo("org1");
    assertThat(task.getUuid()).isEqualTo("UUID_1");
    assertThat(task.getType()).isEqualTo("TYPE_1");
    assertThat(task.getSubmitterUuid()).isEqualTo("LOGIN_1");
    assertThat(task.getComponent()).contains(component);
    assertThat(task.getMainComponent()).contains(mainComponent);
    assertThat(task.getCharacteristics()).containsExactly(entry("k1", "v1"), entry("k2", "v2"));
  }

  @Test
  public void empty_in_submitterLogin_is_considered_as_null() {
    CeTask ceTask = underTest.setOrganizationUuid("org1").setUuid("uuid").setType("type")
      .setSubmitterUuid("")
      .build();

    assertThat(ceTask.getSubmitterUuid()).isNull();
  }

  @Test
  public void equals_and_hashCode_on_uuid() {
    underTest.setOrganizationUuid("org1").setType("TYPE_1").setUuid("UUID_1");
    CeTask task1 = underTest.build();
    CeTask task1bis = underTest.build();
    CeTask task2 = new CeTask.Builder().setOrganizationUuid("org1").setType("TYPE_1").setUuid("UUID_2").build();

    assertThat(task1.equals(task1)).isTrue();
    assertThat(task1.equals(task1bis)).isTrue();
    assertThat(task1.equals(task2)).isFalse();
    assertThat(task1.hashCode()).isEqualTo(task1.hashCode());
    assertThat(task1.hashCode()).isEqualTo(task1bis.hashCode());
  }

  @Test
  public void setCharacteristics_null_is_considered_as_empty() {
    CeTask task = underTest.setOrganizationUuid("org1").setType("TYPE_1").setUuid("UUID_1")
      .setCharacteristics(null)
      .build();
    assertThat(task.getCharacteristics()).isEmpty();
  }
}
