from typing import Callable
from pathlib import Path
from enum import Enum
from typing import NamedTuple
from collections import Counter
import time
import asyncio
import aiohttp
import tqdm

POP20_CC = ('CN IN US ID BR PK NG BD RU JP '
            'MX PH VN ET EG DE IR TR CD FR').split()
BASE_URL = 'http://fluentpython.com/data/flags'
DEST_DIR = Path('downloaded')

DEFAULT_CONCUR_REQ = 3
MAX_CONCUR_REQ = 100

class HttpStatus(Enum):
    OK = 1
    NOT_FOUND = 2
    ERROR = 3
    
class Result(NamedTuple):
    status: str
    country_code: str

class FetchError(Exception):
    def __init__(self, cc):
        self.country_code = cc

def download_many(cc_list: list[str]) -> int:
    return asyncio.run(supervisor(cc_list, DEFAULT_CONCUR_REQ, verbose=True))

async def supervisor(cc_list: list[str], concur_req: int, verbose: bool = False) -> int:
    counter: Counter[HttpStatus] = Counter()
    semaphore = asyncio.Semaphore(concur_req)
    async with aiohttp.ClientSession() as session:
        to_do = [download_one(session, cc, semaphore, verbose) for cc in sorted(cc_list)]
        to_do_iter = asyncio.as_completed(to_do)
        if not verbose:
            to_do_iter = tqdm.tqdm(to_do_iter, total=len(cc_list))

        for coro in to_do_iter:
            try:
                res = await coro
            except FetchError as exc:
                country_code = exc.country_code
                status = HttpStatus.ERROR
                try:
                    error_msg = str(exc.__cause__)
                except AttributeError:
                    error_msg = 'unknown'
                if verbose and error_msg:
                    print(f'*** Error for {country_code}: {error_msg}')
            else:
                status = res.status
            counter[status] += 1
    return counter

async def download_one(session: aiohttp.ClientSession, cc: str, semaphore: asyncio.Semaphore, verbose: bool = False) -> Result:
    msg = ''
    try:
        async with semaphore:
            image = await get_flag(session, cc)
    except aiohttp.ClientResponseError as exc:
        if exc.status == 404:
            status = HttpStatus.NOT_FOUND
            msg = 'not found'
        else:
            raise FetchError(cc) from exc
    except Exception as exc:
        raise FetchError(cc) from exc
    else:
        save_flag(f'{cc}.gif', image)
        status = HttpStatus.OK
        msg = 'ok'
    finally:
        if verbose and msg:
            print(cc, msg)
    return Result(status, cc)

async def get_flag(session: aiohttp.ClientSession, cc: str) -> bytes:
    url = f'{BASE_URL}/{cc}/{cc}.gif'.lower()
    async with session.get(url) as resp:
        if resp.status == 200:
            return await resp.read()
        else:
            resp.raise_for_status()
            return bytes()

def save_flag(filename: str, data: bytes):
    (DEST_DIR / filename).write_bytes(data)

def main(downloader: Callable[[list[str]], int]):
    t0 = time.perf_counter()
    count = downloader(POP20_CC)
    elapsed = time.perf_counter() - t0
    print(f'{count} downloads in {elapsed:.2f}s')

if __name__ == '__main__':
    main(download_many)