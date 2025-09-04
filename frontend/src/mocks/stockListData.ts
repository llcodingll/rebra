export interface StockListItem {
  rank: number;
  name: string;
  code: string;
  price: number;
  change: number;
  changePercent: boolean;
  volume: string;
  logo: string;
  isFavorite: boolean;
}

export interface SimpleNewsItem {
  title: string;
  time: string;
}

import imgImage11 from "figma:asset/5eb297e5fb5eb77d91e0dc360108bf8fa1a12b20.png";
import imgImage12 from "figma:asset/54116abb0da9577e8e87663b82466c2e36792369.png";

export const stockListData: StockListItem[] = [
  {
    rank: 1,
    name: '삼성전자',
    code: '005930',
    price: 71400,
    change: 1.71,
    changePercent: true,
    volume: '1234억',
    logo: imgImage12,
    isFavorite: true
  },
  {
    rank: 2,
    name: '종목2',
    code: '000000',
    price: 71400,
    change: -0.14,
    changePercent: false,
    volume: '1111억',
    logo: imgImage11,
    isFavorite: false
  },
  {
    rank: 3,
    name: '종목3',
    code: '000000',
    price: 71400,
    change: 1.71,
    changePercent: true,
    volume: '999억',
    logo: imgImage11,
    isFavorite: false
  },
  {
    rank: 4,
    name: '종목4',
    code: '000000',
    price: 71400,
    change: 1.71,
    changePercent: true,
    volume: '888억',
    logo: imgImage11,
    isFavorite: false
  },
  {
    rank: 5,
    name: '종목5',
    code: '000000',
    price: 71400,
    change: -7.39,
    changePercent: false,
    volume: '777억',
    logo: imgImage12,
    isFavorite: true
  },
  {
    rank: 6,
    name: '종목6',
    code: '000000',
    price: 71400,
    change: 1.71,
    changePercent: true,
    volume: '666억',
    logo: imgImage11,
    isFavorite: false
  },
  {
    rank: 7,
    name: '종목7',
    code: '000000',
    price: 71400,
    change: 1.71,
    changePercent: true,
    volume: '555억',
    logo: imgImage11,
    isFavorite: false
  },
  {
    rank: 8,
    name: '종목8',
    code: '000000',
    price: 71400,
    change: 1.71,
    changePercent: true,
    volume: '444억',
    logo: imgImage11,
    isFavorite: false
  },
  {
    rank: 9,
    name: '종목9',
    code: '000000',
    price: 71400,
    change: 1.71,
    changePercent: true,
    volume: '333억',
    logo: imgImage11,
    isFavorite: false
  },
  {
    rank: 10,
    name: '종목10',
    code: '000000',
    price: 71400,
    change: -12.71,
    changePercent: false,
    volume: '222억',
    logo: imgImage11,
    isFavorite: false
  }
];

export const simpleNewsData: SimpleNewsItem[] = [
  {
    title: '이곳은 뜨끈한 뉴스가 들어갈 곳 제목이 들어간다. 최대 두 줄...',
    time: '1분 전'
  },
  {
    title: '이곳은 뜨끈한 뉴스가 들어갈 곳 제목이 들어간다. 최대 두 줄...',
    time: '2분 전'
  },
  {
    title: '이곳은 뜨끈한 뉴스가 들어갈 곳 제목이 들어간다. 최대 두 줄...',
    time: '2분 전'
  },
  {
    title: '이곳은 뜨끈한 뉴스가 들어갈 곳 제목이 들어간다. 최대 두 줄...',
    time: '2분 전'
  },
  {
    title: '이곳은 뜨끈한 뉴스가 들어갈 곳 제목이 들어간다. 최대 두 줄...',
    time: '2분 전'
  }
];