/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.api.utils.crypto;

import com.alesharik.webserver.api.utils.crypto.crc.CRC32Provider;
import com.alesharik.webserver.exceptions.error.UnexpectedBehaviorError;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.zip.CRC32;

/**
 * Generate hashes
 */
@Level("Crypto")
@Prefixes("[Hasher]")
@UtilityClass
public class Hasher {
    private static final boolean SHA_512_ENABLED;
    private static final boolean MD5_ENABLED;

    private static final ThreadLocal<MessageDigest> SHA_512_DIGEST;
    private static final ThreadLocal<MessageDigest> MD5_DIGEST;
    private static final ThreadLocal<CRC32> CRC_32 = ThreadLocal.withInitial(CRC32::new);

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("Crypto");

        System.out.println("Hash algorithm checking procedure initiated!");

        {
            boolean sha512;
            try {
                MessageDigest.getInstance("SHA-512");
                sha512 = true;
                System.out.println("SHA512: ok");
            } catch (NoSuchAlgorithmException e) {
                sha512 = false;
                System.out.println("SHA512: fail");
            }
            SHA_512_ENABLED = sha512;
            SHA_512_DIGEST = ThreadLocal.withInitial(() -> {
                if(!isSha512Supported())
                    throw new IllegalStateException("SHA512 hashing algorithm is not supported");

                try {
                    return MessageDigest.getInstance("SHA-512");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return null;
                }
            });
        }
        {
            boolean md5;
            try {
                MessageDigest.getInstance("MD5");
                md5 = true;
                System.out.println("MD5: ok");
            } catch (NoSuchAlgorithmException e) {
                md5 = false;
                System.out.println("MD5: fail");
            }
            MD5_ENABLED = md5;
            MD5_DIGEST = ThreadLocal.withInitial(() -> {
                if(!isMd5Supported())
                    throw new IllegalStateException("MD5 hashing algorithm is not supported");

                try {
                    return MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return null;
                }
            });
        }
        new CRC32Provider();//Init CRC32Provider
        {
            try {
                MessageDigest.getInstance("CRC32");
                System.out.println("CRC32: ok");
            } catch (NoSuchAlgorithmException e) {
                throw new UnexpectedBehaviorError("CRC32 algorithm didn't register itself!", e);
            }
        }
    }

    /**
     * Return true if SHA512 algorithm is supported
     */
    public static boolean isSha512Supported() {
        return SHA_512_ENABLED;
    }

    /**
     * Return true if MD5 algorithm is supported
     */
    public static boolean isMd5Supported() {
        return MD5_ENABLED;
    }

    /**
     * Generate hash in isolated {@link MessageDigest}
     * @param data the data to hash
     * @param salt the salt
     * @return data hash
     * @throws IllegalStateException if current JVM not support SHA512
     */
    @SneakyThrows//Never throw an exception
    public static byte[] securedSHA512(byte[] data, byte[] salt) {
        if(!isSha512Supported())
            throw new IllegalStateException("SHA512 hashing algorithm is not supported");

         MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
         messageDigest.update(salt);
         return messageDigest.digest(data);
    }

    /**
     * Generate hash in thread-local {@link MessageDigest}
     * @param data the data to hash
     * @param salt the salt
     * @return data hash
     * @throws IllegalStateException if current JVM not support SHA512
     */
    public static byte[] SHA512(byte[] data, byte[] salt) {
        if(!isSha512Supported())
            throw new IllegalStateException("SHA512 hashing algorithm is not supported");

        MessageDigest messageDigest = SHA_512_DIGEST.get();
        try {
            messageDigest.update(salt);
            return messageDigest.digest(data);
        } finally {
            messageDigest.reset();
        }
    }

    /**
     * Generate hash in isolated {@link MessageDigest}
     * @param data the data to hash
     * @param salt the salt
     * @return data hash
     * @throws IllegalStateException if current JVM not support MD5
     */
    @SneakyThrows//Never throw an exception
    public static byte[] securedMD5(byte[] data, byte[] salt) {
        if(!isMd5Supported())
            throw new IllegalStateException("MD5 hashing algorithm is not supported");

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(salt);
        messageDigest.update(data);
        return messageDigest.digest();
    }

    /**
     * Generate hash in thread-local {@link MessageDigest}
     * @param data the data to hash
     * @param salt the salt
     * @return data hash
     * @throws IllegalStateException if current JVM not support MD5
     */
    public static byte[] MD5(byte[] data, byte[] salt) {
        if(!isMd5Supported())
            throw new IllegalStateException("MD5 hashing algorithm is not supported");

        MessageDigest messageDigest = MD5_DIGEST.get();
        try {
            messageDigest.update(salt);
            messageDigest.update(data);
            return messageDigest.digest();
        } finally {
            messageDigest.reset();
        }
    }


    /**
     * Generate hash in thread-local {@link MessageDigest}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size
     * @param stream data stream to hash
     * @param salt the salt
     * @return data hash
     * @throws IllegalStateException if current JVM not support MD5
     */
    public static byte[] MD5(InputStream stream, byte[] salt) {
        if(!isMd5Supported())
            throw new IllegalStateException("MD5 hashing algorithm is not supported");

        MessageDigest messageDigest = MD5_DIGEST.get();
        try {
            messageDigest.update(salt);

            byte[] buf = new byte[4096];
            int nRead = stream.read(buf, 0, 4096);
            do {
                messageDigest.update(buf, 0, nRead);
            } while((nRead = stream.read(buf, 0, 4096)) == 4096);

            return messageDigest.digest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            messageDigest.reset();
        }
    }

    /**
     * Generate hash in isolated {@link MessageDigest}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size. It will be executed in {@link ForkJoinPool#commonPool()}
     * @param stream data stream to hash
     * @param salt the salt
     * @return data hash
     * @throws IllegalStateException if current JVM not support MD5
     */
    public static CompletableFuture<byte[]> MD5Async(InputStream stream, byte[] salt) {
        return MD5Async(stream, salt, ForkJoinPool.commonPool());
    }

    /**
     * Generate hash in isolated {@link MessageDigest}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size
     *
     * @param stream   data stream to hash
     * @param salt     the salt
     * @param executor where task should be executed
     * @return data hash
     * @throws IllegalStateException if current JVM not support MD5
     */
    public static CompletableFuture<byte[]> MD5Async(InputStream stream, byte[] salt, Executor executor) {
        if(!isMd5Supported())
            throw new IllegalStateException("MD5 hashing algorithm is not supported");

        return CompletableFuture.supplyAsync(() -> {
            MessageDigest messageDigest;
            try {
                messageDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            try {
                messageDigest.update(salt);

                byte[] buf = new byte[4096];
                int nRead = stream.read(buf, 0, 4096);
                do {
                    messageDigest.update(buf, 0, nRead);
                } while((nRead = stream.read(buf, 0, 4096)) == 4096);

                return messageDigest.digest();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                messageDigest.reset();
            }
        }, executor);
    }

    /**
     * Generate hash thread-local {@link MessageDigest}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size
     * @param file the file to hash
     * @param salt the salt
     * @return data hash
     * @throws IllegalStateException if current JVM not support MD5
     */
    public static byte[] MD5(File file, byte[] salt) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            return MD5(stream, salt);
        }
    }

    /**
     * Generate hash in isolated {@link MessageDigest}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size
     * @param file the file to hash
     * @param salt the salt
     * @param executor where task should be executed
     * @return data hash
     * @throws IllegalStateException if current JVM not support MD5
     */
    public static CompletableFuture<byte[]> MD5Async(File file, byte[] salt, Executor executor) throws FileNotFoundException {
        if(!isMd5Supported())
            throw new IllegalStateException("MD5 hashing algorithm is not supported");

        FileInputStream stream = new FileInputStream(file);
        return MD5Async(stream, salt, executor)
                .thenApply((bytes) -> {
                    try {
                        stream.close();
                        return bytes;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Generate hash in isolated {@link MessageDigest}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size. It will be executed in {@link ForkJoinPool#commonPool()}
     * @param file the file to hash
     * @param salt the salt
     * @return data hash
     * @throws IllegalStateException if current JVM not support MD5
     */
    public static CompletableFuture<byte[]> MD5Async(File file, byte[] salt) throws FileNotFoundException {
        return MD5Async(file, salt, ForkJoinPool.commonPool());
    }

    /**
     * Generate hash in isolated {@link CRC32}
     * @param data the data to hash
     * @param salt the salt
     * @return data hash
     */
    @SneakyThrows//Never throw an exception
    public static long securedCRC32(byte[] data, byte[] salt) {
        CRC32 crc32 = new CRC32();
        crc32.update(salt);
        crc32.update(data);
        return crc32.getValue();
    }

    /**
     * Generate hash in thread-local {@link CRC32}
     * @param data the data to hash
     * @param salt the salt
     * @return data hash
     */
    public static long CRC32(byte[] data, byte[] salt) {
        CRC32 messageDigest = CRC_32.get();
        try {
            messageDigest.update(salt);
            messageDigest.update(data);
            return messageDigest.getValue();
        } finally {
            messageDigest.reset();
        }
    }

    /**
     * Generate hash in thread-local {@link CRC32}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size
     * @param stream data stream to hash
     * @param salt the salt
     * @return data hash
     */
    public static long CRC32(InputStream stream, byte[] salt) {
        CRC32 messageDigest = CRC_32.get();
        try {
            messageDigest.update(salt);

            byte[] buf = new byte[4096];
            int nRead = stream.read(buf, 0, 4096);
            do {
                messageDigest.update(buf, 0, nRead);
            } while((nRead = stream.read(buf, 0, 4096)) == 4096);

            return messageDigest.getValue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            messageDigest.reset();
        }
    }

    /**
     * Generate hash in isolated {@link CRC32}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size. It will be executed in {@link ForkJoinPool#commonPool()}
     * @param stream data stream to hash
     * @param salt the salt
     * @return data hash
     */
    public static CompletableFuture<Long> CRC32Async(InputStream stream, byte[] salt) {
        return CRC32Async(stream, salt, ForkJoinPool.commonPool());
    }

    /**
     * Generate hash in isolated {@link CRC32}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size
     *
     * @param stream   data stream to hash
     * @param salt     the salt
     * @param executor where task should be executed
     * @return data hash
     */
    public static CompletableFuture<Long> CRC32Async(InputStream stream, byte[] salt, Executor executor) {
        if(!isMd5Supported())
            throw new IllegalStateException("MD5 hashing algorithm is not supported");

        return CompletableFuture.supplyAsync(() -> {
            CRC32 messageDigest = new CRC32();
            try {
                messageDigest.update(salt);

                byte[] buf = new byte[4096];
                int nRead = stream.read(buf, 0, 4096);
                do {
                    messageDigest.update(buf, 0, nRead);
                } while((nRead = stream.read(buf, 0, 4096)) == 4096);

                return messageDigest.getValue();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                messageDigest.reset();
            }
        }, executor);
    }

    /**
     * Generate hash thread-local {@link CRC32}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size
     * @param file the file to hash
     * @param salt the salt
     * @return data hash
     */
    public static long CRC32(File file, byte[] salt) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            return CRC32(stream, salt);
        }
    }

    /**
     * Generate hash in isolated {@link CRC32}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size
     * @param file the file to hash
     * @param salt the salt
     * @param executor where task should be executed
     * @return data hash
     */
    public static CompletableFuture<Long> CRC32Async(File file, byte[] salt, Executor executor) throws FileNotFoundException {
        FileInputStream stream = new FileInputStream(file);
        return CRC32Async(stream, salt, executor)
                .thenApply((bytes) -> {
                    try {
                        stream.close();
                        return bytes;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Generate hash in isolated {@link CRC32}. It will stop getting data from {@link InputStream} when it read less
     * than buffer size. It will be executed in {@link ForkJoinPool#commonPool()}
     * @param file the file to hash
     * @param salt the salt
     * @return data hash
     */
    public static CompletableFuture<Long> CRC32Async(File file, byte[] salt) throws FileNotFoundException {
        return CRC32Async(file, salt, ForkJoinPool.commonPool());
    }
}
