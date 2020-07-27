/*
 *
 * Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package android.hardware;

import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * sub class
 */
public class SerialPort {

    public SerialPort(String name) {
        throw new RuntimeException();
    }

    public void open(ParcelFileDescriptor pfd, int speed) throws IOException {
        throw new RuntimeException();
    }

    public void close() throws IOException {
        throw new RuntimeException();
    }

    public String getName() {
        throw new RuntimeException();
    }

    public int read(ByteBuffer buffer, int offset) throws IOException {
        throw new RuntimeException();
    }

    public void write(ByteBuffer buffer, int length) throws IOException {
        throw new RuntimeException();
    }

    public void sendBreak() {
        throw new RuntimeException();
    }
}
