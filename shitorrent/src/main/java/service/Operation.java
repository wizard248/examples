package service;

import model.ResourceHandle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public enum Operation {
    HELP {
        @Override
        public void perform(final FrontService service, final BufferedReader reader, final BufferedWriter writer) throws IOException {
            writer.write(COMMANDS);
            writer.write(CRLF);

            for (final Operation operation : Operation.values()) {
                writer.write(operation.name());

                for (String argument : operation.getArguments()) {
                    writer.write(' ');
                    writer.write(argument);
                }

                writer.write(CRLF);
            }

            writer.flush();
        }
    },
    LIST_RESOURCES {
        @Override
        public void perform(final FrontService service, final BufferedReader reader, final BufferedWriter writer) throws IOException {
            final List<String> resourceIds = service.listResourceIds();

            if (resourceIds.isEmpty()) {
                writer.write(NOTHING);
                writer.write(CRLF);
            } else {
                for (final String resourceId : resourceIds) {
                    writer.write(resourceId);
                    writer.write(CRLF);
                }
            }

            writer.flush();
        }
    },
    LIST_PEERS {
        @Override
        public void perform(final FrontService service, final BufferedReader reader, final BufferedWriter writer) throws IOException {
            final String resourceId = reader.readLine();

            for (final InetAddress peerAddress : service.listPeers(resourceId)) {
                writer.write(peerAddress.getCanonicalHostName());
                writer.write(CRLF);
            }

            writer.flush();
        }

        @Override
        public List<String> getArguments() {
            return Arrays.asList("resource_id");
        }
    },
    LIST_CHUNKS {
        @Override
        public void perform(final FrontService service, final BufferedReader reader, final BufferedWriter writer) throws IOException {
            final String resourceId = reader.readLine();
            writer.write(service.listChunks(resourceId));
            writer.write(CRLF);
            writer.flush();
        }

        @Override
        public List<String> getArguments() {
            return Arrays.asList("resource_id");
        }
    },
    GET_CHUNK {
        @Override
        public void perform(final FrontService service, final BufferedReader reader, final BufferedWriter writer) throws IOException {
            final String resourceId = reader.readLine();
            final int chunkIndex = Integer.parseInt(reader.readLine());
            final byte[] data = service.getChunk(resourceId, chunkIndex);
            writer.write(new String(Base64.getEncoder().encode(data)));
            writer.write(CRLF);
            writer.flush();
        }

        @Override
        public List<String> getArguments() {
            return Arrays.asList("resource_id", "chunk_index");
        }
    },
    SEED {
        @Override
        public void perform(final FrontService service, final BufferedReader reader, final BufferedWriter writer) throws IOException {
            final String path = reader.readLine();
            final ResourceHandle handle = service.seed(Paths.get(path));
            writer.write(handle.getSeederAddress().getCanonicalHostName());
            writer.write(CRLF);
            writer.write(handle.getResourceId());
            writer.write(CRLF);
            writer.write(String.valueOf(handle.getChunkSize()));
            writer.write(CRLF);
            writer.write(String.valueOf(handle.getFileSize()));
            writer.write(CRLF);
            writer.flush();
        }

        @Override
        public List<String> getArguments() {
            return Arrays.asList("path");
        }
    },
    LEECH {
        @Override
        public void perform(final FrontService service, final BufferedReader reader, final BufferedWriter writer) throws IOException {
            final ResourceHandle handle = new ResourceHandle(
                    InetAddress.getByName(reader.readLine()),
                    reader.readLine(),
                    Integer.parseInt(reader.readLine()),
                    Long.parseLong(reader.readLine())
            );
            final Path targetFile = Paths.get(reader.readLine());
            service.leech(handle, targetFile);
            writer.write(OK);
            writer.write(CRLF);
            writer.flush();
        }

        @Override
        public List<String> getArguments() {
            return Arrays.asList("seeder_host", "resource_id", "chunk_size", "file_size", "target_path");
        }
    };

    private static final String CRLF = "\r\n";
    private static final String OK = "OK";
    private static final String COMMANDS = "=== list of supported commands ===";
    private static final String NOTHING = "-----";

    public static void findAndPerform(final FrontService service, final BufferedReader reader, final BufferedWriter writer) throws IOException {
        while (true) {
            final String operationName = reader.readLine();

            if (operationName == null) {
                break;
            }

            try {
                final Operation operation = Operation.valueOf(operationName);
                operation.perform(service, reader, writer);
            } catch (Exception e) {
                writer.write(e.getClass().getName());
                writer.write(CRLF);
                writer.write(e.getMessage());
                writer.write(CRLF);
                writer.flush();
            }
        }
    }

    public List<String> getArguments() {
        return Collections.emptyList();
    }

    abstract public void perform(final FrontService service, final BufferedReader reader, final BufferedWriter writer) throws IOException;
}
