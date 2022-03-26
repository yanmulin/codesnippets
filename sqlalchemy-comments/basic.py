from datetime import datetime
import sqlalchemy
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column
from sqlalchemy.types import DateTime, Integer, String
from sqlalchemy.orm import sessionmaker
from faker import Faker

eng = create_engine("sqlite:///:memory:")

Base = declarative_base()

Session = sessionmaker(bind=eng)

faker = Faker()

class Comment(Base):
    __tablename__ = "comments"
    id = Column(Integer, primary_key=True)
    text = Column(String(140))
    author = Column(String(32))
    timestamp = Column(DateTime(), default=datetime.utcnow)

Base.metadata.bind = eng
Base.metadata.create_all()

def insert_fake_data(session, num_rows):
    for _ in range(num_rows):
        session.add(Comment(text=faker.text(100), author=faker.name()))
    session.commit()

def display_comments():
    session = Session()
    for c in session.query(Comment).all():
        print("%s%s %s by %s" % ("", c.text, c.timestamp.date(), c.author))

if __name__ == "__main__":
    insert_fake_data(Session(), 100)
    display_comments()