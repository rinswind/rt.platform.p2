/*******************************************************************************
 * Copyright (c) 2014, 2021 EclipseSource and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     EclipseSource - initial API and implementation
 *     Todor Boev - refactor to the java 7 file api
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.touchpoint.natives;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.equinox.internal.p2.touchpoint.natives.SimpleBackupStore;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;

public class SimpleBackupStoreTest extends AbstractProvisioningTest {
	private static final String BACKUP_PREFIX = "backup-test";

	private Path sourceDir;

	private Path aDir;
	private Path aaDir;
	private Path aFile;

	/**
	 * <pre>
	 * /p2-backup-test
	 *   /a
	 *     /aa
	 *       /eclipse.exe
	 * </pre>
	 */
	@Override
	public void setUp() throws IOException {
		String userHome = System.getProperty("user.home");

		sourceDir = Path.of(userHome, "p2-backup-test");
		deleteAll(sourceDir);

		aDir = sourceDir.resolve("a");
		Files.createDirectories(aDir);

		aaDir = aDir.resolve("aa");
		Files.createDirectories(aaDir);

		// The eclipse.exe is the only one eligible for backup-in-place
		aFile = aaDir.resolve("eclipse.exe");
		Files.createFile(aFile);
	}

	@Override
	public void tearDown() throws IOException {
		deleteAll(sourceDir);
	}

	private static void deleteAll(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			for (Path child : Files.list(path).collect(toList())) {
				deleteAll(child);
			}
		}

		Files.deleteIfExists(path);
	}

	public void testBackupInPlace() throws IOException {
		class TestMoveInPlaceStore extends SimpleBackupStore {
			public TestMoveInPlaceStore() {
				super(null, BACKUP_PREFIX);
			}

			@Override
			public Path toInPlaceBackupPath(Path path) {
				return super.toInPlaceBackupPath(path);
			}

			@Override
			public void move(Path a, Path b) throws IOException {
				// In place backup - allow
				if (b.equals(toInPlaceBackupPath(a))) {
					super.move(a, b);
				}
				// In place restore - allow
				else if (a.equals(toInPlaceBackupPath(b))) {
					super.move(a, b);
				}
				// Everything else - fail
				else {
					throw new IOException("Test fail move: " + a + " -> " + b);
				}
			}
		}

		TestMoveInPlaceStore buStore = new TestMoveInPlaceStore();

		final Path path = aFile;
		final Path inPlaceBuPath = buStore.toInPlaceBackupPath(aFile);

		buStore.backup(path.toFile());

		assertFalse(Files.exists(path));
		assertTrue(Files.exists(inPlaceBuPath));

		buStore.restore();

		assertTrue(Files.exists(path));
		assertFalse(Files.exists(inPlaceBuPath));
	}

	public void testNoBackupInPlace() throws IOException {
		class TestNoBackupInPlaceStore extends SimpleBackupStore {
			public TestNoBackupInPlaceStore() {
				super(null, BACKUP_PREFIX);
			}

			@Override
			public Path toInPlaceBackupPath(Path path) {
				return super.toInPlaceBackupPath(path);
			}
		}

		TestNoBackupInPlaceStore buStore = new TestNoBackupInPlaceStore();

		final Path path = aFile;
		final Path inPlaceBuPath = buStore.toInPlaceBackupPath(aFile);

		buStore.backup(path.toFile());

		assertFalse(Files.exists(path));
		assertFalse(Files.exists(inPlaceBuPath));

		buStore.restore();

		assertTrue(Files.exists(path));
	}
}
