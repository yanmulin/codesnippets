from typing import Callable
from pathlib import Path
import time
import asyncio
from aiohttp import ClientSession

POP20_CC = ('CN IN US ID BR PK NG BD RU JP '
            'MX PH VN ET EG DE IR TR CD FR').split()
BASE_URL = 'http://fluentpython.com/data/flags'
DEST_DIR = Path('downloaded')

def download_many(cc_list: list[str]) -> int:
    return asyncio.run(supervisor(cc_list))

async def supervisor(cc_list: list[str]) -> int:
    async with ClientSession() as session:
        to_do = [download_one(session, cc) for cc in sorted(cc_list)]
        res = await asyncio.gather(*to_do)
    return len(res)

async def download_one(session: ClientSession, cc: str) -> str:
    image = await get_flag(session, cc)
    save_flag(f'{cc}.gif', image)
    print(cc, end=' ', flush=True)
    return cc

async def get_flag(session: ClientSession, cc: str) -> bytes:
    url = f'{BASE_URL}/{cc}/{cc}.gif'.lower()
    async with session.get(url) as resp:
        return await resp.read()

def save_flag(filename: str, data: bytes):
    (DEST_DIR / filename).write_bytes(data)

def main(downloader: Callable[[list[str]], int]):
    t0 = time.perf_counter()
    count = downloader(POP20_CC)
    elapsed = time.perf_counter() - t0
    print(f'\n{count} downloads in {elapsed:.2f}s')
    
if __name__ == '__main__':
    main(download_many)