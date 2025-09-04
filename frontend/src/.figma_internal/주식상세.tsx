import imgChart from "figma:asset/7cbfeb4cacad8b7ff05087e6bb2f76c3ba4d921e.png";
import imgImage19 from "figma:asset/a35cbf8e782d1162ac0360ad37c72b41b56ed0c6.png";
import imgImage20 from "figma:asset/3a465bc3eceefca059097f79720cc85a3b28b734.png";
import { imgVector, imgVector1, imgVector2, imgVector3, imgVector4, imgVector5, imgVector6, imgVector7, imgVector8, imgSvg, imgSvg1, imgVector9, imgVector10 } from "./svg-k4lth";

function Container() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-nowrap text-white">
        <p className="leading-[17.5px] whitespace-pre">R</p>
      </div>
    </div>
  );
}

function Background() {
  return (
    <div className="bg-[#030213] box-border content-stretch flex items-center justify-center pb-[5.25px] pt-[4.25px] px-0 relative rounded-[6.75px] shrink-0 size-10" data-name="Background">
      <Container />
    </div>
  );
}

function Margin() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[7px] py-0 relative shrink-0 size-10" data-name="Margin">
      <Background />
    </div>
  );
}

function Container1() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-3 pr-[30px] py-0 relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[22px] text-neutral-950 text-nowrap">
        <p className="leading-[24.5px] whitespace-pre">ReBalance Pro</p>
      </div>
    </div>
  );
}

function Frame() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-5" data-name="Frame">
      <div className="absolute inset-[12.5%]" data-name="Vector">
        <div className="absolute inset-[-3.89%]">
          <img className="block max-w-none size-full" src={imgVector} />
        </div>
      </div>
      <div className="absolute bottom-[29.17%] left-3/4 right-1/4 top-[37.5%]" data-name="Vector">
        <div className="absolute inset-[-8.75%_-0.58px]">
          <img className="block max-w-none size-full" src={imgVector1} />
        </div>
      </div>
      <div className="absolute inset-[20.83%_45.83%_29.17%_54.17%]" data-name="Vector">
        <div className="absolute inset-[-5.83%_-0.58px]">
          <img className="block max-w-none size-full" src={imgVector2} />
        </div>
      </div>
      <div className="absolute inset-[58.33%_66.67%_29.17%_33.33%]" data-name="Vector">
        <div className="absolute inset-[-23.33%_-0.58px]">
          <img className="block max-w-none size-full" src={imgVector3} />
        </div>
      </div>
    </div>
  );
}

function Svg() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-5" data-name="SVG">
      <Frame />
    </div>
  );
}

function SvgMargin() {
  return (
    <div className="box-border content-stretch flex flex-col h-5 items-center justify-center pb-0 pt-[3px] px-0 relative shrink-0 w-[30px]" data-name="SVG:margin">
      <Svg />
    </div>
  );
}

function Container2() {
  return (
    <div className="box-border content-stretch flex flex-col items-center justify-center pb-0 pt-[3px] px-0 relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#b7b7b7] text-[16px] text-center text-nowrap">
        <p className="leading-[0px] whitespace-pre">대시보드</p>
      </div>
    </div>
  );
}

function Button() {
  return (
    <div className="box-border content-stretch flex h-[31.5px] items-center justify-start pb-2 pt-[5px] px-[10.5px] relative shrink-0 w-28" data-name="Button">
      <SvgMargin />
      <Container2 />
    </div>
  );
}

function ButtonMargin() {
  return (
    <div className="box-border content-stretch flex flex-col h-[31.5px] items-start justify-start pb-0 pl-0 pr-[3.5px] pt-[5px] relative shrink-0" data-name="Button:margin">
      <Button />
    </div>
  );
}

function Frame1() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-5" data-name="Frame">
      <div className="absolute inset-[12.5%_20.83%_20.83%_12.5%]" data-name="Vector">
        <div className="absolute inset-[-4.375%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
      <div className="absolute inset-[69.58%_12.5%_12.5%_69.58%]" data-name="Vector">
        <div className="absolute inset-[-16.279%]">
          <img className="block max-w-none size-full" src={imgVector5} />
        </div>
      </div>
    </div>
  );
}

function Svg1() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-5" data-name="SVG">
      <Frame1 />
    </div>
  );
}

function SvgMargin1() {
  return (
    <div className="box-border content-stretch flex flex-col h-5 items-start justify-start pl-0 pr-[15px] py-0 relative shrink-0 w-[25px]" data-name="SVG:margin">
      <Svg1 />
    </div>
  );
}

function Container3() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#030213] text-[16px] text-center text-nowrap">
        <p className="leading-[0px] whitespace-pre">주식 검색</p>
      </div>
    </div>
  );
}

function Button1() {
  return (
    <div className="box-border content-stretch flex h-[31.5px] items-center justify-center pb-2 pl-[5px] pr-[10.5px] pt-[5px] relative shrink-0" data-name="Button">
      <SvgMargin1 />
      <Container3 />
    </div>
  );
}

function ButtonMargin1() {
  return (
    <div className="box-border content-stretch flex flex-col h-[31.5px] items-start justify-start pb-0 pl-0 pr-[3.5px] pt-[5px] relative shrink-0" data-name="Button:margin">
      <Button1 />
    </div>
  );
}

function Frame2() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-5" data-name="Frame">
      <div className="absolute inset-[8.33%_39.58%]" data-name="Vector">
        <div className="absolute inset-[-3.5%_-14%]">
          <img className="block max-w-none size-full" src={imgVector6} />
        </div>
      </div>
      <div className="absolute inset-[8.33%_35.42%_91.67%_35.42%]" data-name="Vector">
        <div className="absolute inset-[-0.58px_-10%]">
          <img className="block max-w-none size-full" src={imgVector7} />
        </div>
      </div>
      <div className="absolute inset-[66.67%_39.58%_33.33%_39.58%]" data-name="Vector">
        <div className="absolute inset-[-0.58px_-14%]">
          <img className="block max-w-none size-full" src={imgVector8} />
        </div>
      </div>
    </div>
  );
}

function Svg2() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-5" data-name="SVG">
      <Frame2 />
    </div>
  );
}

function SvgMargin2() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 size-5" data-name="SVG:margin">
      <Svg2 />
    </div>
  );
}

function Container4() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#b7b7b7] text-[16px] text-center text-nowrap">
        <p className="leading-[0px] whitespace-pre">백테스트</p>
      </div>
    </div>
  );
}

function Button2() {
  return (
    <div className="box-border content-stretch flex h-[31.5px] items-center justify-center pb-2 pl-[5px] pr-[10.5px] pt-[5px] relative shrink-0" data-name="Button">
      <SvgMargin2 />
      <Container4 />
    </div>
  );
}

function ButtonMargin2() {
  return (
    <div className="box-border content-stretch flex flex-col h-[31.5px] items-start justify-start pb-0 pl-0 pr-[3.5px] pt-[5px] relative shrink-0" data-name="Button:margin">
      <Button2 />
    </div>
  );
}

function Container5() {
  return (
    <div className="box-border content-stretch flex items-center justify-start pl-2.5 pr-0 py-0 relative shrink-0" data-name="Container">
      <Margin />
      <Container1 />
      <ButtonMargin />
      <ButtonMargin1 />
      <ButtonMargin2 />
    </div>
  );
}

function Svg3() {
  return (
    <div className="relative shrink-0 size-8" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg} />
    </div>
  );
}

function Background1() {
  return (
    <div className="absolute bg-[#d4183d] box-border content-stretch flex items-center justify-center overflow-clip pb-[2.25px] pt-[1.25px] px-px right-[-3.5px] rounded-[3.35544e+07px] size-[17.5px] top-[-3.5px]" data-name="Background">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[10.5px] text-center text-nowrap text-white">
        <p className="leading-[14px] whitespace-pre">3</p>
      </div>
    </div>
  );
}

function Button3() {
  return (
    <div className="box-border content-stretch flex h-7 items-center justify-center px-[8.75px] py-0 relative rounded-[6.75px] shrink-0" data-name="Button">
      <Svg3 />
      <Background1 />
    </div>
  );
}

function ButtonMargin3() {
  return (
    <div className="box-border content-stretch flex flex-col h-7 items-start justify-start pl-0 pr-5 py-0 relative shrink-0" data-name="Button:margin">
      <Button3 />
    </div>
  );
}

function Svg4() {
  return (
    <div className="relative shrink-0 size-8" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg1} />
    </div>
  );
}

function Button4() {
  return (
    <div className="box-border content-stretch flex h-7 items-center justify-center px-[8.75px] py-0 relative rounded-[6.75px] shrink-0" data-name="Button">
      <Svg4 />
    </div>
  );
}

function ButtonMargin4() {
  return (
    <div className="box-border content-stretch flex flex-col h-7 items-start justify-start pl-0 pr-5 py-0 relative shrink-0" data-name="Button:margin">
      <Button4 />
    </div>
  );
}

function Container6() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 z-[1]" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[20px] text-neutral-950 text-nowrap">
        <p className="leading-[21px] whitespace-pre">김투자</p>
      </div>
    </div>
  );
}

function Container7() {
  return (
    <div className="box-border content-stretch flex isolate items-center justify-start pl-0 pr-5 py-0 relative shrink-0" data-name="Container">
      <Container6 />
    </div>
  );
}

function Container8() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <ButtonMargin3 />
      <ButtonMargin4 />
      <Container7 />
    </div>
  );
}

function Container9() {
  return (
    <div className="content-stretch flex items-center justify-between relative shrink-0 w-full" data-name="Container">
      <Container5 />
      <Container8 />
    </div>
  );
}

function Header() {
  return (
    <div className="absolute bg-white box-border content-stretch flex flex-col items-start justify-start left-0 px-[21px] py-5 top-0 w-[1920px]" data-name="Header">
      <div aria-hidden="true" className="absolute border-[0px_0px_1px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <Container9 />
    </div>
  );
}

function Margin1() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[67px]" data-name="Margin">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[20px] w-[77px]">
        <p className="leading-[21px]">KOSPI</p>
      </div>
    </div>
  );
}

function Margin2() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[16px] text-neutral-950 w-[78px]">
        <p className="leading-[21px]">2,486.669</p>
      </div>
    </div>
  );
}

function Frame3() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[10.5px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector9} />
        </div>
      </div>
      <div className="absolute inset-[45.83%_8.33%_29.17%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector10} />
        </div>
      </div>
    </div>
  );
}

function Svg5() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[10.5px]" data-name="SVG">
      <Frame3 />
    </div>
  );
}

function SvgMargin3() {
  return (
    <div className="box-border content-stretch flex flex-col h-[10.5px] items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0 w-3.5" data-name="SVG:margin">
      <Svg5 />
    </div>
  );
}

function Margin3() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[16px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">-8.45</p>
      </div>
    </div>
  );
}

function Container10() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[16px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">(-0.31%)</p>
      </div>
    </div>
  );
}

function Container11() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <SvgMargin3 />
      <Margin3 />
      <Container10 />
    </div>
  );
}

function Container12() {
  return (
    <div className="basis-0 content-stretch flex grow items-center justify-start min-h-px min-w-px relative shrink-0" data-name="Container">
      <Margin1 />
      <Margin2 />
      <Container11 />
    </div>
  );
}

function Margin4() {
  return (
    <div className="box-border content-stretch flex flex-col h-full items-start justify-center min-w-[279.2px] pl-0 pr-7 py-0 relative shrink-0" data-name="Margin">
      <Container12 />
    </div>
  );
}

function Margin5() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[20px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">KOSDAQ</p>
      </div>
    </div>
  );
}

function Margin6() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[16px] text-neutral-950 text-nowrap">
        <p className="leading-[21px] whitespace-pre">742.308</p>
      </div>
    </div>
  );
}

function Frame4() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[10.5px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector9} />
        </div>
      </div>
      <div className="absolute inset-[45.83%_8.33%_29.17%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector10} />
        </div>
      </div>
    </div>
  );
}

function Svg6() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[10.5px]" data-name="SVG">
      <Frame4 />
    </div>
  );
}

function SvgMargin4() {
  return (
    <div className="box-border content-stretch flex flex-col h-[10.5px] items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0 w-3.5" data-name="SVG:margin">
      <Svg6 />
    </div>
  );
}

function Margin7() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">-3.39</p>
      </div>
    </div>
  );
}

function Container13() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">(-0.73%)</p>
      </div>
    </div>
  );
}

function Container14() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <SvgMargin4 />
      <Margin7 />
      <Container13 />
    </div>
  );
}

function Container15() {
  return (
    <div className="basis-0 content-stretch flex grow items-center justify-start min-h-px min-w-px relative shrink-0" data-name="Container">
      <Margin5 />
      <Margin6 />
      <Container14 />
    </div>
  );
}

function Margin8() {
  return (
    <div className="box-border content-stretch flex flex-col h-full items-start justify-center min-w-[279.06px] pl-0 pr-7 py-0 relative shrink-0" data-name="Margin">
      <Container15 />
    </div>
  );
}

function Margin9() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">KOSPI200</p>
      </div>
    </div>
  );
}

function Margin10() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-neutral-950 text-nowrap">
        <p className="leading-[21px] whitespace-pre">338.582</p>
      </div>
    </div>
  );
}

function Frame5() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[10.5px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector9} />
        </div>
      </div>
      <div className="absolute inset-[45.83%_8.33%_29.17%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector10} />
        </div>
      </div>
    </div>
  );
}

function Svg7() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[10.5px]" data-name="SVG">
      <Frame5 />
    </div>
  );
}

function SvgMargin5() {
  return (
    <div className="box-border content-stretch flex flex-col h-[10.5px] items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0 w-3.5" data-name="SVG:margin">
      <Svg7 />
    </div>
  );
}

function Margin11() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">-3.94</p>
      </div>
    </div>
  );
}

function Container16() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">(-0.90%)</p>
      </div>
    </div>
  );
}

function Container17() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <SvgMargin5 />
      <Margin11 />
      <Container16 />
    </div>
  );
}

function Container18() {
  return (
    <div className="basis-0 content-stretch flex grow items-center justify-start min-h-px min-w-px relative shrink-0" data-name="Container">
      <Margin9 />
      <Margin10 />
      <Container17 />
    </div>
  );
}

function Margin12() {
  return (
    <div className="box-border content-stretch flex flex-col h-full items-start justify-center min-w-[285.75px] pl-0 pr-7 py-0 relative shrink-0" data-name="Margin">
      <Container18 />
    </div>
  );
}

function Margin13() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">USD/KRW</p>
      </div>
    </div>
  );
}

function Margin14() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-neutral-950 text-nowrap">
        <p className="leading-[21px] whitespace-pre">1,347.836</p>
      </div>
    </div>
  );
}

function Frame6() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[10.5px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector9} />
        </div>
      </div>
      <div className="absolute inset-[45.83%_8.33%_29.17%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector10} />
        </div>
      </div>
    </div>
  );
}

function Svg8() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[10.5px]" data-name="SVG">
      <Frame6 />
    </div>
  );
}

function SvgMargin6() {
  return (
    <div className="box-border content-stretch flex flex-col h-[10.5px] items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0 w-3.5" data-name="SVG:margin">
      <Svg8 />
    </div>
  );
}

function Margin15() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">-5.12</p>
      </div>
    </div>
  );
}

function Container19() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">(-0.36%)</p>
      </div>
    </div>
  );
}

function Container20() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <SvgMargin6 />
      <Margin15 />
      <Container19 />
    </div>
  );
}

function Container21() {
  return (
    <div className="basis-0 content-stretch flex grow items-center justify-start min-h-px min-w-px relative shrink-0" data-name="Container">
      <Margin13 />
      <Margin14 />
      <Container20 />
    </div>
  );
}

function Margin16() {
  return (
    <div className="box-border content-stretch flex flex-col h-full items-start justify-center min-w-[303.66px] pl-0 pr-7 py-0 relative shrink-0" data-name="Margin">
      <Container21 />
    </div>
  );
}

function Container22() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pb-1 pt-[3px] px-0 relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#6a7282] text-[10.5px] text-nowrap">
        <p className="leading-[14px] whitespace-pre">• 10초마다 자동 업데이트</p>
      </div>
    </div>
  );
}

function Container23() {
  return (
    <div className="content-stretch flex h-[68px] items-start justify-start overflow-auto relative shrink-0 w-full" data-name="Container">
      <Margin4 />
      <Margin8 />
      <Margin12 />
      <Margin16 />
      <Container22 />
    </div>
  );
}

function BackgroundHorizontalBorder() {
  return (
    <div className="absolute bg-gray-50 box-border content-stretch flex flex-col items-start justify-start left-0 pb-[11.5px] pt-[10.5px] px-[21px] right-0 top-20" data-name="Background+HorizontalBorder">
      <div aria-hidden="true" className="absolute border-[0px_0px_1px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <Container23 />
    </div>
  );
}

function Group18() {
  return (
    <div className="absolute contents left-[33px] top-[363px]">
      <div className="absolute bg-center bg-cover bg-no-repeat h-[693px] left-[33px] rounded-[20px] top-[363px] w-[847.592px]" data-name="Chart" style={{ backgroundImage: `url('${imgChart}')` }}>
        <div aria-hidden="true" className="absolute border-4 border-[#dfdfdf] border-solid inset-0 pointer-events-none rounded-[20px]" />
      </div>
      <div className="absolute bg-center bg-cover bg-no-repeat h-[693px] left-[1483px] rounded-[20px] top-[363px] w-[401px]" data-name="image 19" style={{ backgroundImage: `url('${imgImage19}')` }}>
        <div aria-hidden="true" className="absolute border-4 border-[#dfdfdf] border-solid inset-0 pointer-events-none rounded-[20px]" />
      </div>
      <div className="absolute bg-center bg-cover bg-no-repeat h-[693px] left-[914px] rounded-[20px] top-[363px] w-[545px]" data-name="image 20" style={{ backgroundImage: `url('${imgImage20}')` }}>
        <div aria-hidden="true" className="absolute border-4 border-[#dfdfdf] border-solid inset-0 pointer-events-none rounded-[20px]" />
      </div>
    </div>
  );
}

function Product() {
  return (
    <div className="absolute contents leading-[0] left-[54px] text-nowrap top-[237px]" data-name="Product">
      <div className="absolute flex flex-col font-['Sen:Bold',_'Noto_Sans_KR:Bold',_sans-serif] font-bold justify-center text-[#3d3d3d] text-[36px] text-center translate-x-[-50%] translate-y-[-50%]" style={{ top: "calc(50% - 294px)", left: "calc(50% - 839.5px)" }}>
        <p className="leading-[14px] text-nowrap whitespace-pre">삼성전자</p>
      </div>
      <div className="absolute font-['Consolas:Bold',_'Noto_Sans_KR:Bold',_sans-serif] left-14 not-italic text-[#3d3d3d] text-[48px] top-[298px]">
        <p className="leading-[24px] text-nowrap whitespace-pre">71,400원</p>
      </div>
      <div className="absolute font-['Consolas:Bold',_sans-serif] left-[196px] not-italic text-[#acacac] text-[32px] top-[237px]">
        <p className="leading-[24px] text-nowrap whitespace-pre">005930</p>
      </div>
      <div className="absolute font-['Consolas:Bold',_'Noto_Sans_KR:Bold',_sans-serif] left-[277px] not-italic text-[#ef1515] text-[32px] top-[299px]">
        <p className="leading-[24px] text-nowrap whitespace-pre">+7,000원(12.2%)</p>
      </div>
    </div>
  );
}

export default function Component() {
  return (
    <div className="bg-white relative size-full" data-name="주식상세">
      <Header />
      <BackgroundHorizontalBorder />
      <Group18 />
      <Product />
    </div>
  );
}