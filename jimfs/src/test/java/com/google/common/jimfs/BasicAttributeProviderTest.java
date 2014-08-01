/*
 * Copyright 2013 Google Inc.
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

package com.google.common.jimfs;

import static org.truth0.Truth.ASSERT;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Set;

/**
 * Tests for {@link BasicAttributeProvider}.
 *
 * @author Colin Decker
 */
@RunWith(JUnit4.class)
public class BasicAttributeProviderTest extends
    AbstractAttributeProviderTest<BasicAttributeProvider> {

  @Override
  protected BasicAttributeProvider createProvider() {
    return new BasicAttributeProvider();
  }

  @Override
  protected Set<? extends AttributeProvider> createInheritedProviders() {
    return ImmutableSet.of();
  }

  @Test
  public void testSupportedAttributes() {
    assertSupportsAll("fileKey", "size", "isDirectory", "isRegularFile", "isSymbolicLink",
        "isOther", "creationTime", "lastModifiedTime", "lastAccessTime");
  }

  @Test
  public void testInitialAttributes() {
    long time = file.getCreationTime();
    ASSERT.that(time).isNotEqualTo(0L);
    ASSERT.that(time).isEqualTo(file.getLastAccessTime());
    ASSERT.that(time).isEqualTo(file.getLastModifiedTime());

    assertContainsAll(file,
        ImmutableMap.<String, Object>builder()
            .put("fileKey", 0)
            .put("size", 0L)
            .put("isDirectory", true)
            .put("isRegularFile", false)
            .put("isSymbolicLink", false)
            .put("isOther", false)
            .build());
  }

  @Test
  public void testSet() {
    FileTime time = FileTime.fromMillis(0L);

    // settable
    assertSetAndGetSucceeds("creationTime", time);
    assertSetAndGetSucceeds("lastModifiedTime", time);
    assertSetAndGetSucceeds("lastAccessTime", time);

    // unsettable
    assertSetFails("fileKey", 3L);
    assertSetFails("size", 1L);
    assertSetFails("isRegularFile", true);
    assertSetFails("isDirectory", true);
    assertSetFails("isSymbolicLink", true);
    assertSetFails("isOther", true);

    // invalid type
    assertSetFails("creationTime", "foo");
  }

  @Test
  public void testSetOnCreate() {
    FileTime time = FileTime.fromMillis(0L);

    assertSetFailsOnCreate("creationTime", time);
    assertSetFailsOnCreate("lastModifiedTime", time);
    assertSetFailsOnCreate("lastAccessTime", time);
  }

  @Test
  public void testView() throws IOException {
    BasicFileAttributeView view = provider.view(fileLookup(), NO_INHERITED_VIEWS);

    ASSERT.that(view).isNotNull();
    ASSERT.that(view.name()).isEqualTo("basic");

    BasicFileAttributes attrs = view.readAttributes();
    ASSERT.that(attrs.fileKey()).isEqualTo(0);

    FileTime time = attrs.creationTime();
    ASSERT.that(attrs.lastAccessTime()).isEqualTo(time);
    ASSERT.that(attrs.lastModifiedTime()).isEqualTo(time);

    view.setTimes(null, null, null);

    attrs = view.readAttributes();
    ASSERT.that(attrs.creationTime()).isEqualTo(time);
    ASSERT.that(attrs.lastAccessTime()).isEqualTo(time);
    ASSERT.that(attrs.lastModifiedTime()).isEqualTo(time);

    view.setTimes(FileTime.fromMillis(0L), null, null);

    attrs = view.readAttributes();
    ASSERT.that(attrs.creationTime()).isEqualTo(time);
    ASSERT.that(attrs.lastAccessTime()).isEqualTo(time);
    ASSERT.that(attrs.lastModifiedTime()).isEqualTo(FileTime.fromMillis(0L));
  }

  @Test
  public void testAttributes() {
    BasicFileAttributes attrs = provider.readAttributes(file);
    ASSERT.that(attrs.fileKey()).isEqualTo(0);
    ASSERT.that(attrs.isDirectory()).isTrue();
    ASSERT.that(attrs.isRegularFile()).isFalse();
    ASSERT.that(attrs.creationTime()).isNotNull();
  }
}