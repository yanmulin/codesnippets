import asyncio
import logging
from asyncio import StreamReader, StreamWriter

logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO)

class Connection:
    def __init__(self, reader: StreamReader, writer: StreamWriter):
        self.reader = reader
        self.writer = writer

    @property
    def addr(self) -> None:
        return self.writer.get_extra_info("peername")


async def handle_echo(reader: StreamReader, writer: StreamWriter):
    conn = Connection(reader, writer)
    data = await conn.reader.readline()
    message = data.decode()
    logger.info(f"Recived {message} from {conn.addr}")

    writer.write(data)
    await writer.drain()
    writer.close()
    await writer.wait_closed()


async def main():
    server = await asyncio.start_server(
        handle_echo,
        "127.0.0.1",
        8888,
    )

    addrs = ', '.join(str(sock.getsockname()) for sock in server.sockets)
    logger.info(f'Serving on {addrs}')

    async with server:
        await server.serve_forever()


asyncio.run(main())
