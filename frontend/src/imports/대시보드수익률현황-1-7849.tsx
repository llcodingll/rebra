import imgPhoto14720996457855658Abf4Ff4E from "figma:asset/f2e0d0183a438e31fe7131ed2173548b7f21aea2.png";
import imgImage3 from "figma:asset/10ab81539875bd08fb11acc6c58753b6c244c1e0.png";
import imgImage22 from "figma:asset/1ea006dc4cf62fda2e8ddd425d0178712455c473.png";
import { imgSvg, imgSvg1, imgVector, imgVector1, imgVector2, imgVector3, imgVector4, imgVector5, imgVector6, imgVector7, imgVector8, imgVector9, imgVector10, imgFrame, imgVector11, imgVector12, imgVector13, imgVector14, imgVector15 } from "./svg-fecjf";

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
    <div className="bg-[#030213] box-border content-stretch flex items-center justify-center pb-[5.25px] pt-[4.25px] px-0 relative rounded-[6.75px] shrink-0 size-7" data-name="Background">
      <Container />
    </div>
  );
}

function Margin() {
  return (
    <div className="box-border content-stretch flex flex-col h-7 items-start justify-start pl-0 pr-[7px] py-0 relative shrink-0 w-[35px]" data-name="Margin">
      <Background />
    </div>
  );
}

function Container1() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[15.8px] text-neutral-950 text-nowrap">
        <p className="leading-[24.5px] whitespace-pre">ReBalance Pro</p>
      </div>
    </div>
  );
}

function Container2() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <Margin />
      <Container1 />
    </div>
  );
}

function Svg() {
  return (
    <div className="relative shrink-0 size-3.5" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg} />
    </div>
  );
}

function Button() {
  return (
    <div className="box-border content-stretch flex h-7 items-center justify-center px-[8.75px] py-0 relative rounded-[6.75px] shrink-0" data-name="Button">
      <Svg />
    </div>
  );
}

function ButtonMargin() {
  return (
    <div className="absolute box-border content-stretch flex flex-col h-7 items-start justify-start left-[45.5px] pl-0 pr-3.5 py-0 top-0" data-name="Button:margin">
      <Button />
    </div>
  );
}

function Svg1() {
  return (
    <div className="relative shrink-0 size-3.5" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg1} />
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

function Button1() {
  return (
    <div className="box-border content-stretch flex h-7 items-center justify-center px-[8.75px] py-0 relative rounded-[6.75px] shrink-0" data-name="Button">
      <Svg1 />
      <Background1 />
    </div>
  );
}

function ButtonMargin1() {
  return (
    <div className="absolute box-border content-stretch flex flex-col h-7 items-start justify-start left-0 pl-0 pr-3.5 py-0 top-0" data-name="Button:margin">
      <Button1 />
    </div>
  );
}

function Photo14720996457855658Abf4Ff4E() {
  return <div className="basis-0 bg-no-repeat bg-size-[100%_100%] bg-top-left grow h-full max-w-7 min-h-px min-w-px shrink-0" data-name="photo-1472099645785-5658abf4ff4e" style={{ backgroundImage: `url('${imgPhoto14720996457855658Abf4Ff4E}')` }} />;
}

function Container3() {
  return (
    <div className="content-stretch flex items-start justify-center overflow-clip relative rounded-[3.35544e+07px] shrink-0 size-7" data-name="Container">
      <Photo14720996457855658Abf4Ff4E />
    </div>
  );
}

function Margin1() {
  return (
    <div className="box-border content-stretch flex flex-col h-7 items-start justify-start pl-0 pr-[7px] py-0 relative shrink-0 w-[35px] z-[2]" data-name="Margin">
      <Container3 />
    </div>
  );
}

function Container4() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start min-w-[42px] relative shrink-0 z-[1]" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-neutral-950 text-nowrap">
        <p className="leading-[21px] whitespace-pre">김투자</p>
      </div>
    </div>
  );
}

function Container5() {
  return (
    <div className="absolute content-stretch flex isolate items-center justify-start left-[91px] top-1/2 translate-y-[-50%]" data-name="Container">
      <Margin1 />
      <Container4 />
    </div>
  );
}

function Container6() {
  return (
    <div className="h-7 relative shrink-0 w-[168px]" data-name="Container">
      <ButtonMargin />
      <ButtonMargin1 />
      <Container5 />
    </div>
  );
}

function Container7() {
  return (
    <div className="content-stretch flex items-center justify-between relative shrink-0 w-full" data-name="Container">
      <Container2 />
      <Container6 />
    </div>
  );
}

function Header() {
  return (
    <div className="bg-white relative shrink-0 w-full" data-name="Header">
      <div aria-hidden="true" className="absolute border-[0px_0px_1px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col items-start justify-start pb-[15px] pt-3.5 px-[21px] relative w-full">
          <Container7 />
        </div>
      </div>
    </div>
  );
}

function Frame() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute inset-[12.5%]" data-name="Vector">
        <div className="absolute inset-[-5.56%]">
          <img className="block max-w-none size-full" src={imgVector} />
        </div>
      </div>
      <div className="absolute bottom-[29.17%] left-3/4 right-1/4 top-[37.5%]" data-name="Vector">
        <div className="absolute inset-[-12.5%_-0.58px]">
          <img className="block max-w-none size-full" src={imgVector1} />
        </div>
      </div>
      <div className="absolute inset-[20.83%_45.83%_29.17%_54.17%]" data-name="Vector">
        <div className="absolute inset-[-8.33%_-0.58px]">
          <img className="block max-w-none size-full" src={imgVector2} />
        </div>
      </div>
      <div className="absolute inset-[58.33%_66.67%_29.17%_33.33%]" data-name="Vector">
        <div className="absolute inset-[-33.33%_-0.58px]">
          <img className="block max-w-none size-full" src={imgVector3} />
        </div>
      </div>
    </div>
  );
}

function Svg2() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame />
    </div>
  );
}

function SvgMargin() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[7px] py-0 relative shrink-0 w-[21px]" data-name="SVG:margin">
      <Svg2 />
    </div>
  );
}

function Container8() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start min-w-[49px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-center text-neutral-950 text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">대시보드</p>
      </div>
    </div>
  );
}

function Button2() {
  return (
    <div className="box-border content-stretch flex gap-[7px] h-[31.5px] items-center justify-center pb-2 pt-[5px] px-[10.5px] relative shrink-0" data-name="Button">
      <SvgMargin />
      <Container8 />
    </div>
  );
}

function ButtonMargin2() {
  return (
    <div className="box-border content-stretch flex flex-col h-[31.5px] items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0" data-name="Button:margin">
      <Button2 />
    </div>
  );
}

function Frame1() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute inset-[12.5%_20.83%_20.83%_12.5%]" data-name="Vector">
        <div className="absolute inset-[-6.25%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
      <div className="absolute inset-[69.58%_12.5%_12.5%_69.58%]" data-name="Vector">
        <div className="absolute inset-[-23.256%]">
          <img className="block max-w-none size-full" src={imgVector5} />
        </div>
      </div>
    </div>
  );
}

function Svg3() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame1 />
    </div>
  );
}

function SvgMargin1() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[7px] py-0 relative shrink-0 w-[21px]" data-name="SVG:margin">
      <Svg3 />
    </div>
  );
}

function Container9() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start min-w-[52.41px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-center text-neutral-950 text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">주식 검색</p>
      </div>
    </div>
  );
}

function Button3() {
  return (
    <div className="box-border content-stretch flex gap-[7px] h-[31.5px] items-center justify-center pb-2 pt-[5px] px-[10.5px] relative shrink-0" data-name="Button">
      <SvgMargin1 />
      <Container9 />
    </div>
  );
}

function ButtonMargin3() {
  return (
    <div className="box-border content-stretch flex flex-col h-[31.5px] items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0" data-name="Button:margin">
      <Button3 />
    </div>
  );
}

function Frame2() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute inset-[8.33%_39.58%]" data-name="Vector">
        <div className="absolute inset-[-5%_-20%]">
          <img className="block max-w-none size-full" src={imgVector6} />
        </div>
      </div>
      <div className="absolute inset-[8.33%_35.42%_91.67%_35.42%]" data-name="Vector">
        <div className="absolute inset-[-0.58px_-14.29%]">
          <img className="block max-w-none size-full" src={imgVector7} />
        </div>
      </div>
      <div className="absolute inset-[66.67%_39.58%_33.33%_39.58%]" data-name="Vector">
        <div className="absolute inset-[-0.58px_-20%]">
          <img className="block max-w-none size-full" src={imgVector8} />
        </div>
      </div>
    </div>
  );
}

function Svg4() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame2 />
    </div>
  );
}

function SvgMargin2() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[7px] py-0 relative shrink-0 w-[21px]" data-name="SVG:margin">
      <Svg4 />
    </div>
  );
}

function Container10() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start min-w-[49px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-center text-neutral-950 text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">백테스트</p>
      </div>
    </div>
  );
}

function Button4() {
  return (
    <div className="box-border content-stretch flex gap-[7px] h-[31.5px] items-center justify-center pb-2 pt-[5px] px-[10.5px] relative shrink-0" data-name="Button">
      <SvgMargin2 />
      <Container10 />
    </div>
  );
}

function Container11() {
  return (
    <div className="content-stretch flex items-start justify-start relative shrink-0 w-full" data-name="Container">
      <ButtonMargin2 />
      <ButtonMargin3 />
      <Button4 />
    </div>
  );
}

function Nav() {
  return (
    <div className="bg-white relative shrink-0 w-full" data-name="Nav">
      <div aria-hidden="true" className="absolute border-[0px_0px_1px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col items-start justify-start pb-px pt-0 px-[21px] relative w-full">
          <Container11 />
        </div>
      </div>
    </div>
  );
}

function Margin2() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[67px]" data-name="Margin">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[20px] w-[77px]">
        <p className="leading-[21px]">KOSPI</p>
      </div>
    </div>
  );
}

function Margin3() {
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

function Margin4() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[16px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">-8.45</p>
      </div>
    </div>
  );
}

function Container12() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[16px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">(-0.31%)</p>
      </div>
    </div>
  );
}

function Container13() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <SvgMargin3 />
      <Margin4 />
      <Container12 />
    </div>
  );
}

function Container14() {
  return (
    <div className="basis-0 content-stretch flex grow items-center justify-start min-h-px min-w-px relative shrink-0" data-name="Container">
      <Margin2 />
      <Margin3 />
      <Container13 />
    </div>
  );
}

function Margin5() {
  return (
    <div className="box-border content-stretch flex flex-col h-full items-start justify-center min-w-[279.2px] pl-0 pr-7 py-0 relative shrink-0" data-name="Margin">
      <Container14 />
    </div>
  );
}

function Margin6() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[20px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">KOSDAQ</p>
      </div>
    </div>
  );
}

function Margin7() {
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

function Margin8() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">-3.39</p>
      </div>
    </div>
  );
}

function Container15() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">(-0.73%)</p>
      </div>
    </div>
  );
}

function Container16() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <SvgMargin4 />
      <Margin8 />
      <Container15 />
    </div>
  );
}

function Container17() {
  return (
    <div className="basis-0 content-stretch flex grow items-center justify-start min-h-px min-w-px relative shrink-0" data-name="Container">
      <Margin6 />
      <Margin7 />
      <Container16 />
    </div>
  );
}

function Margin9() {
  return (
    <div className="box-border content-stretch flex flex-col h-full items-start justify-center min-w-[279.06px] pl-0 pr-7 py-0 relative shrink-0" data-name="Margin">
      <Container17 />
    </div>
  );
}

function Margin10() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">KOSPI200</p>
      </div>
    </div>
  );
}

function Margin11() {
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

function Margin12() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">-3.94</p>
      </div>
    </div>
  );
}

function Container18() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">(-0.90%)</p>
      </div>
    </div>
  );
}

function Container19() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <SvgMargin5 />
      <Margin12 />
      <Container18 />
    </div>
  );
}

function Container20() {
  return (
    <div className="basis-0 content-stretch flex grow items-center justify-start min-h-px min-w-px relative shrink-0" data-name="Container">
      <Margin10 />
      <Margin11 />
      <Container19 />
    </div>
  );
}

function Margin13() {
  return (
    <div className="box-border content-stretch flex flex-col h-full items-start justify-center min-w-[285.75px] pl-0 pr-7 py-0 relative shrink-0" data-name="Margin">
      <Container20 />
    </div>
  );
}

function Margin14() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">USD/KRW</p>
      </div>
    </div>
  );
}

function Margin15() {
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

function Margin16() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">-5.12</p>
      </div>
    </div>
  );
}

function Container21() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">(-0.36%)</p>
      </div>
    </div>
  );
}

function Container22() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <SvgMargin6 />
      <Margin16 />
      <Container21 />
    </div>
  );
}

function Container23() {
  return (
    <div className="basis-0 content-stretch flex grow items-center justify-start min-h-px min-w-px relative shrink-0" data-name="Container">
      <Margin14 />
      <Margin15 />
      <Container22 />
    </div>
  );
}

function Margin17() {
  return (
    <div className="box-border content-stretch flex flex-col h-full items-start justify-center min-w-[303.66px] pl-0 pr-7 py-0 relative shrink-0" data-name="Margin">
      <Container23 />
    </div>
  );
}

function Container24() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pb-1 pt-[3px] px-0 relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#6a7282] text-[10.5px] text-nowrap">
        <p className="leading-[14px] whitespace-pre">• 10초마다 자동 업데이트</p>
      </div>
    </div>
  );
}

function Container25() {
  return (
    <div className="content-stretch flex h-[68px] items-start justify-start overflow-auto relative shrink-0 w-full" data-name="Container">
      <Margin5 />
      <Margin9 />
      <Margin13 />
      <Margin17 />
      <Container24 />
    </div>
  );
}

function BackgroundHorizontalBorder() {
  return (
    <div className="bg-gray-50 relative shrink-0 w-full" data-name="Background+HorizontalBorder">
      <div aria-hidden="true" className="absolute border-[0px_0px_1px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.5px] pt-[10.5px] px-[21px] relative w-full">
          <Container25 />
        </div>
      </div>
    </div>
  );
}

function Frame8() {
  return (
    <div className="absolute left-[55px] size-[19px] top-[61px]" data-name="Frame">
      <img className="block max-w-none size-full" src={imgFrame} />
    </div>
  );
}

function Frame20() {
  return (
    <div className="h-[109px] overflow-clip relative shrink-0 w-[264px]">
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[31px] not-italic text-[#4a5565] text-[16px] text-nowrap top-[7px]">
        <p className="leading-[24px] whitespace-pre">즉시 실행</p>
      </div>
      <div className="absolute bg-[#155dfc] h-[45px] left-[31px] rounded-[8px] shadow-[0px_2px_4px_0px_rgba(21,93,252,0.2),0px_0px_0px_0px_rgba(0,0,0,0),0px_0px_0px_0px_rgba(0,0,0,0),0px_0px_0px_0px_rgba(0,0,0,0),0px_0px_0px_0px_rgba(0,0,0,0)] top-12 w-[206px]" data-name="Rectangle" />
      <Frame8 />
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[149.5px] not-italic text-[16px] text-center text-nowrap text-white top-[60px] translate-x-[-50%]">
        <p className="leading-[21px] whitespace-pre">지금 리벨런싱 실행</p>
      </div>
    </div>
  );
}

function Frame19() {
  return (
    <div className="h-[81px] overflow-clip relative shrink-0 w-[137px]">
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-3 not-italic text-[#4a5565] text-[16px] text-nowrap top-[7px]">
        <p className="leading-[24px] whitespace-pre">자동 리벨런싱</p>
      </div>
      <div className="absolute bg-[#155dfc] h-[21px] left-3 rounded-[1.78957e+07px] top-12 w-[43px]" data-name="Rectangle" />
      <div className="absolute bg-white left-[35px] rounded-[1.78957e+07px] shadow-[0px_3.568px_5.352px_0px_rgba(0,0,0,0.1),0px_1.784px_3.568px_0px_rgba(0,0,0,0.1),0px_0px_0px_0px_rgba(0,0,0,0),0px_0px_0px_0px_rgba(0,0,0,0),0px_0px_0px_0px_rgba(0,0,0,0),0px_0px_0px_0px_rgba(0,0,0,0)] size-[18px] top-[49px]" data-name="Rectangle" />
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[71px] not-italic text-[#155dfc] text-[17px] text-nowrap top-[51px]">
        <p className="leading-[21px] whitespace-pre">활성화</p>
      </div>
    </div>
  );
}

function Group26() {
  return (
    <div className="absolute contents left-[362px] top-[52px]">
      <div className="absolute bg-[#155dfc] h-[21px] left-[362px] rounded-[3px] top-[52px] w-[66px]" data-name="Rectangle" />
      <div className="absolute font-['Inter:Medium',_'Noto_Sans_KR:Regular',_sans-serif] font-medium leading-[0] left-[397px] not-italic text-[12px] text-center text-white top-[59px] translate-x-[-50%] w-[25px]">
        <p className="leading-[10px]">저장</p>
      </div>
    </div>
  );
}

function Frame18() {
  return (
    <div className="h-[86px] overflow-clip relative shrink-0 w-[428px]">
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-8 not-italic text-[#4a5565] text-[16px] text-nowrap top-0">
        <p className="leading-[26px] whitespace-pre">리밸런싱 주기</p>
      </div>
      <div className="absolute bg-[#f8f9fa] h-[35px] left-8 rounded-[4px] top-[43px] w-[47px]" data-name="Rectangle">
        <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[4px]" />
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[55.5px] not-italic text-[#4a5565] text-[12px] text-center text-nowrap top-[51px] translate-x-[-50%]">
        <p className="leading-[18px] whitespace-pre">주간</p>
      </div>
      <div className="absolute bg-[#155dfc] h-[35px] left-[87px] rounded-[4px] top-[43px] w-[47px]" data-name="Rectangle">
        <div aria-hidden="true" className="absolute border border-[#155dfc] border-solid inset-0 pointer-events-none rounded-[4px]" />
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[110.5px] not-italic text-[12px] text-center text-nowrap text-white top-[51px] translate-x-[-50%]">
        <p className="leading-[18px] whitespace-pre">월간</p>
      </div>
      <div className="absolute bg-[#f8f9fa] h-[35px] left-[142px] rounded-[4px] top-[43px] w-[47px]" data-name="Rectangle">
        <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[4px]" />
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[165.5px] not-italic text-[#4a5565] text-[12px] text-center text-nowrap top-[51px] translate-x-[-50%]">
        <p className="leading-[18px] whitespace-pre">연간</p>
      </div>
      <div className="absolute bg-[#f8f9fa] h-[37px] left-[220.62px] rounded-[4px] top-[46.21px] w-[59px]" data-name="Rectangle">
        <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[4px]" />
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[299.62px] not-italic text-[#4a5565] text-[13px] text-nowrap top-[50.21px]">
        <p className="leading-[20px] whitespace-pre">개월마다</p>
      </div>
      <Group26 />
    </div>
  );
}

function Container26() {
  return (
    <div className="absolute content-stretch flex gap-[90px] h-[76px] items-center justify-center left-[-241px] top-[204.5px] w-[1571px]" data-name="Container">
      <Frame20 />
      <Frame19 />
      <Frame18 />
    </div>
  );
}

function Component() {
  return (
    <div className="absolute font-['Noto_Sans_KR:Regular',_sans-serif] h-[87px] leading-[0] left-[49px] not-italic overflow-clip top-[68.5px] w-[599px]" data-name="포트폴리오 선택">
      <div className="absolute flex flex-col justify-center left-[13px] text-[#1c398e] text-[12.578px] text-nowrap top-[80.5px] translate-y-[-50%]">
        <p className="leading-[12.578px] whitespace-pre">다른 포트폴리오 보기</p>
      </div>
      <div className="absolute flex flex-col justify-center left-[12.58px] text-[28.75px] text-black text-nowrap top-[26.71px] translate-y-[-50%]">
        <p className="leading-[12.578px] whitespace-pre">A 포트폴리오</p>
      </div>
      <div className="absolute flex flex-col h-[38px] justify-center left-[181px] text-[24px] text-black top-7 translate-y-[-50%] w-[418px]">
        <p className="leading-[12.578px]">은퇴 자금 마련</p>
      </div>
    </div>
  );
}

function Heading4() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 4">
      <div className="flex flex-col font-['Nico_Moji:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[23.982px] text-neutral-950 w-full">
        <p className="leading-[16.788px]">등록 주식</p>
      </div>
    </div>
  );
}

function Container27() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col items-start justify-start pb-[6.295px] pt-[25.181px] px-[25.181px] relative w-full">
          <Heading4 />
        </div>
      </div>
    </div>
  );
}

function Cell() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[227.831px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">종목명</p>
      </div>
    </div>
  );
}

function Cell1() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[201.451px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">매수가/현재가</p>
      </div>
    </div>
  );
}

function Cell2() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[204px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">수량/평가금액</p>
      </div>
    </div>
  );
}

function Cell3() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[167.276px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">수익률</p>
      </div>
    </div>
  );
}

function Cell4() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[124px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">현재 비중(%)</p>
      </div>
    </div>
  );
}

function Cell5() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[131.902px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">목표 비중(%)</p>
      </div>
    </div>
  );
}

function Cell6() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[86px]" data-name="Cell">
      <div className="flex flex-col font-['ABeeZee:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">가중치</p>
      </div>
    </div>
  );
}

function Cell7() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[125.907px]" data-name="Cell">
      <div className="flex flex-col font-['ABeeZee:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">임계값 비중(%)</p>
      </div>
    </div>
  );
}

function Cell8() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[112.441px]" data-name="Cell">
      <div className="flex flex-col font-['ABeeZee:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">제외</p>
      </div>
    </div>
  );
}

function HeaderRow() {
  return (
    <div className="content-stretch flex items-start justify-start relative shrink-0 w-full" data-name="Header → Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <Cell />
      <Cell1 />
      <Cell2 />
      <Cell3 />
      <Cell4 />
      <Cell5 />
      <Cell6 />
      <Cell7 />
      <Cell8 />
    </div>
  );
}

function Margin18() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start min-w-[67.15px] pl-0 pr-[8.394px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">삼성전자</p>
      </div>
    </div>
  );
}

function Border() {
  return (
    <div className="relative rounded-[8.094px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[9.593px] py-[3.298px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.591px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[16.788px] whitespace-pre">005930</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8.094px]" />
    </div>
  );
}

function Data() {
  return (
    <div className="content-stretch flex flex-col h-[38px] items-start justify-center relative shrink-0 w-[213px]" data-name="Data">
      <Margin18 />
      <Border />
    </div>
  );
}

function Container28() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">68,000</p>
      </div>
    </div>
  );
}

function Container29() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] w-full">
        <p className="leading-[20.984px]">71,800</p>
      </div>
    </div>
  );
}

function Data1() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[185px]" data-name="Data">
      <Container28 />
      <Container29 />
    </div>
  );
}

function Container30() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">50주</p>
      </div>
    </div>
  );
}

function Container31() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">3,590,000원</p>
      </div>
    </div>
  );
}

function Data2() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[187.769px]" data-name="Data">
      <Container30 />
      <Container31 />
    </div>
  );
}

function Frame9() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[12.591px]" data-name="Frame">
      <div className="absolute inset-[29.16%_8.33%_29.17%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector11} />
        </div>
      </div>
      <div className="absolute inset-[29.16%_8.33%_45.84%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector12} />
        </div>
      </div>
    </div>
  );
}

function Svg9() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[12.591px]" data-name="SVG">
      <Frame9 />
    </div>
  );
}

function SvgMargin7() {
  return (
    <div className="box-border content-stretch flex flex-col h-[12.591px] items-start justify-start pl-0 pr-[4.197px] py-0 relative shrink-0 w-[16.788px]" data-name="SVG:margin">
      <Svg9 />
    </div>
  );
}

function Container32() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">+5.6%</p>
      </div>
    </div>
  );
}

function Container33() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <SvgMargin7 />
      <Container32 />
    </div>
  );
}

function Container34() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.591px] w-full">
        <p className="leading-[16.788px]">(+190,000원)</p>
      </div>
    </div>
  );
}

function Data3() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.092px] pl-0 pr-[8.394px] pt-[9.893px] relative shrink-0 w-[158.882px]" data-name="Data">
      <Container33 />
      <Container34 />
    </div>
  );
}

function Data4() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[106px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">32.1%</p>
      </div>
    </div>
  );
}

function Container35() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start overflow-auto pl-0 pr-[5.24px] py-[1.199px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[normal] whitespace-pre">30</p>
      </div>
    </div>
  );
}

function Container36() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <Container35 />
    </div>
  );
}

function Container37() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pl-0 pr-[17.987px] py-0 relative shrink-0" data-name="Container">
      <Container36 />
    </div>
  );
}

function Input() {
  return (
    <div className="bg-[#f3f3f5] box-border content-stretch flex h-[37.772px] items-start justify-start overflow-clip px-[13.79px] py-[10.492px] relative rounded-[8.094px] shrink-0 w-[67.15px]" data-name="Input">
      <Container37 />
    </div>
  );
}

function Data5() {
  return (
    <div className="box-border content-stretch flex flex-col h-[59.956px] items-start justify-between pl-0 pr-[8.394px] py-[11.092px] relative shrink-0 w-[71.947px]" data-name="Data">
      <Input />
    </div>
  );
}

function Data6() {
  return (
    <div className="box-border content-stretch flex flex-col items-center justify-center pb-[20.085px] pl-5 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[106px]" data-name="Data">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-black text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">6</p>
      </div>
    </div>
  );
}

function Container38() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start overflow-auto pl-0 pr-[5.24px] py-[1.199px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[normal] whitespace-pre">10</p>
      </div>
    </div>
  );
}

function Container39() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <Container38 />
    </div>
  );
}

function Container40() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pl-0 pr-[17.987px] py-0 relative shrink-0" data-name="Container">
      <Container39 />
    </div>
  );
}

function Input1() {
  return (
    <div className="bg-[#f3f3f5] box-border content-stretch flex h-[37.772px] items-center justify-center overflow-clip px-[13.79px] py-[10.492px] relative rounded-[8.094px] shrink-0 w-[67.15px]" data-name="Input">
      <Container40 />
    </div>
  );
}

function Data7() {
  return (
    <div className="box-border content-stretch flex flex-col h-[60px] items-start justify-between pl-0 pr-[8.394px] py-[11.092px] relative shrink-0 w-[78px]" data-name="Data">
      <Input1 />
    </div>
  );
}

function Data8() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[79.141px]" data-name="Data">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="flex-none scale-y-[-100%]">
          <div className="bg-center bg-cover bg-no-repeat size-[20.385px]" data-name="image 3" style={{ backgroundImage: `url('${imgImage3}')` }} />
        </div>
      </div>
    </div>
  );
}

function Row() {
  return (
    <div className="relative shrink-0 w-full" data-name="Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex gap-[16.788px] items-center justify-start pl-[8.394px] pr-0 py-0 relative w-full">
          <Data />
          <Data1 />
          <Data2 />
          <Data3 />
          <Data4 />
          <Data5 />
          <Data6 />
          <Data7 />
          <Data8 />
        </div>
      </div>
    </div>
  );
}

function Margin19() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start min-w-[86.744px] pl-0 pr-[8.394px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">SK하이닉스</p>
      </div>
    </div>
  );
}

function Border1() {
  return (
    <div className="relative rounded-[8.094px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[9.593px] py-[3.298px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.591px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[16.788px] whitespace-pre">000660</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8.094px]" />
    </div>
  );
}

function Data9() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[214px]" data-name="Data">
      <Margin19 />
      <Border1 />
    </div>
  );
}

function Container41() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">85,000</p>
      </div>
    </div>
  );
}

function Container42() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] w-full">
        <p className="leading-[20.984px]">89,500</p>
      </div>
    </div>
  );
}

function Data10() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[183px]" data-name="Data">
      <Container41 />
      <Container42 />
    </div>
  );
}

function Container43() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">30주</p>
      </div>
    </div>
  );
}

function Container44() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">2,685,000원</p>
      </div>
    </div>
  );
}

function Data11() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[187.769px]" data-name="Data">
      <Container43 />
      <Container44 />
    </div>
  );
}

function Frame10() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[12.591px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%_29.16%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector11} />
        </div>
      </div>
      <div className="absolute inset-[29.17%_8.33%_45.83%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector12} />
        </div>
      </div>
    </div>
  );
}

function Svg10() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[12.591px]" data-name="SVG">
      <Frame10 />
    </div>
  );
}

function SvgMargin8() {
  return (
    <div className="box-border content-stretch flex flex-col h-[12.591px] items-start justify-start pl-0 pr-[4.197px] py-0 relative shrink-0 w-[16.788px]" data-name="SVG:margin">
      <Svg10 />
    </div>
  );
}

function Container45() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">+5.3%</p>
      </div>
    </div>
  );
}

function Container46() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <SvgMargin8 />
      <Container45 />
    </div>
  );
}

function Container47() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.591px] w-full">
        <p className="leading-[16.788px]">(+135,000원)</p>
      </div>
    </div>
  );
}

function Data12() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.092px] pl-0 pr-[8.394px] pt-[9.893px] relative shrink-0 w-[158.882px]" data-name="Data">
      <Container46 />
      <Container47 />
    </div>
  );
}

function Data13() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[108px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">24.0%</p>
      </div>
    </div>
  );
}

function Container48() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start overflow-auto pl-0 pr-[5.24px] py-[1.199px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[normal] whitespace-pre">25</p>
      </div>
    </div>
  );
}

function Container49() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <Container48 />
    </div>
  );
}

function Container50() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pl-0 pr-[17.987px] py-0 relative shrink-0" data-name="Container">
      <Container49 />
    </div>
  );
}

function Input2() {
  return (
    <div className="bg-[#f3f3f5] box-border content-stretch flex h-[37.772px] items-start justify-start overflow-clip px-[13.79px] py-[10.492px] relative rounded-[8.094px] shrink-0 w-[67.15px]" data-name="Input">
      <Container50 />
    </div>
  );
}

function Data14() {
  return (
    <div className="box-border content-stretch flex flex-col h-[59.956px] items-start justify-between pl-0 pr-[8.394px] py-[11.092px] relative shrink-0 w-[71.947px]" data-name="Data">
      <Input2 />
    </div>
  );
}

function Data15() {
  return (
    <div className="box-border content-stretch flex flex-col items-center justify-center pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[103px]" data-name="Data">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">5</p>
      </div>
    </div>
  );
}

function Container51() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start overflow-auto pl-0 pr-[5.24px] py-[1.199px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[normal] whitespace-pre">5</p>
      </div>
    </div>
  );
}

function Container52() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <Container51 />
    </div>
  );
}

function Container53() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pl-0 pr-[17.987px] py-0 relative shrink-0" data-name="Container">
      <Container52 />
    </div>
  );
}

function Input3() {
  return (
    <div className="bg-[#f3f3f5] box-border content-stretch flex h-[38px] items-start justify-start overflow-clip px-[13.79px] py-[10.492px] relative rounded-[8.094px] shrink-0 w-20" data-name="Input">
      <Container53 />
    </div>
  );
}

function Data16() {
  return (
    <div className="box-border content-stretch flex flex-col h-[60px] items-start justify-between pl-0 pr-[8.394px] py-[11.092px] relative shrink-0 w-[89px]" data-name="Data">
      <Input3 />
    </div>
  );
}

function Data17() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[93.531px]" data-name="Data">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="flex-none scale-y-[-100%]">
          <div className="bg-center bg-cover bg-no-repeat size-[20.385px]" data-name="image 4" style={{ backgroundImage: `url('${imgImage3}')` }} />
        </div>
      </div>
    </div>
  );
}

function Row1() {
  return (
    <div className="relative shrink-0 w-full" data-name="Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex gap-[16.788px] items-center justify-start pl-[8.394px] pr-0 py-0 relative w-full">
          <Data9 />
          <Data10 />
          <Data11 />
          <Data12 />
          <Data13 />
          <Data14 />
          <Data15 />
          <Data16 />
          <Data17 />
        </div>
      </div>
    </div>
  );
}

function Margin20() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start min-w-[115.51px] pl-0 pr-[8.394px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">LG에너지솔루션</p>
      </div>
    </div>
  );
}

function Border2() {
  return (
    <div className="relative rounded-[8.094px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[9.593px] py-[3.298px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.591px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[16.788px] whitespace-pre">373220</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8.094px]" />
    </div>
  );
}

function Data18() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[211px]" data-name="Data" style={{ gap: "1.70404e-14px" }}>
      <Margin20 />
      <Border2 />
    </div>
  );
}

function Container54() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">390,000</p>
      </div>
    </div>
  );
}

function Container55() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] w-full">
        <p className="leading-[20.984px]">412,000</p>
      </div>
    </div>
  );
}

function Data19() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[188px]" data-name="Data">
      <Container54 />
      <Container55 />
    </div>
  );
}

function Container56() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">15주</p>
      </div>
    </div>
  );
}

function Container57() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">6,180,000원</p>
      </div>
    </div>
  );
}

function Data20() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[187.769px]" data-name="Data">
      <Container56 />
      <Container57 />
    </div>
  );
}

function Frame11() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[12.591px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%_29.16%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector11} />
        </div>
      </div>
      <div className="absolute inset-[29.17%_8.33%_45.83%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector12} />
        </div>
      </div>
    </div>
  );
}

function Svg11() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[12.591px]" data-name="SVG">
      <Frame11 />
    </div>
  );
}

function SvgMargin9() {
  return (
    <div className="box-border content-stretch flex flex-col h-[12.591px] items-start justify-start pl-0 pr-[4.197px] py-0 relative shrink-0 w-[16.788px]" data-name="SVG:margin">
      <Svg11 />
    </div>
  );
}

function Container58() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">+5.6%</p>
      </div>
    </div>
  );
}

function Container59() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <SvgMargin9 />
      <Container58 />
    </div>
  );
}

function Container60() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.591px] w-full">
        <p className="leading-[16.788px]">(+330,000원)</p>
      </div>
    </div>
  );
}

function Data21() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.092px] pl-0 pr-[8.394px] pt-[9.893px] relative shrink-0 w-[158.882px]" data-name="Data">
      <Container59 />
      <Container60 />
    </div>
  );
}

function Data22() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[108px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">18.5%</p>
      </div>
    </div>
  );
}

function Container61() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start overflow-auto pl-0 pr-[5.24px] py-[1.199px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[normal] whitespace-pre">20</p>
      </div>
    </div>
  );
}

function Container62() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <Container61 />
    </div>
  );
}

function Container63() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pl-0 pr-[17.987px] py-0 relative shrink-0" data-name="Container">
      <Container62 />
    </div>
  );
}

function Input4() {
  return (
    <div className="bg-[#f3f3f5] box-border content-stretch flex h-[37.772px] items-start justify-start overflow-clip px-[13.79px] py-[10.492px] relative rounded-[8.094px] shrink-0 w-[67.15px]" data-name="Input">
      <Container63 />
    </div>
  );
}

function Data23() {
  return (
    <div className="box-border content-stretch flex flex-col h-[59.956px] items-start justify-between pl-0 pr-[8.394px] py-[11.092px] relative shrink-0 w-[71.947px]" data-name="Data">
      <Input4 />
    </div>
  );
}

function Data24() {
  return (
    <div className="box-border content-stretch flex flex-col items-center justify-center pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[95.653px]" data-name="Data">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-black text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">4</p>
      </div>
    </div>
  );
}

function Container64() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start overflow-auto pl-0 pr-[5.24px] py-[1.199px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[normal] whitespace-pre">10</p>
      </div>
    </div>
  );
}

function Container65() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <Container64 />
    </div>
  );
}

function Container66() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pl-0 pr-[17.987px] py-0 relative shrink-0" data-name="Container">
      <Container65 />
    </div>
  );
}

function Input5() {
  return (
    <div className="bg-[#f3f3f5] box-border content-stretch flex h-[37.772px] items-start justify-start overflow-clip px-[13.79px] py-[10.492px] relative rounded-[8.094px] shrink-0 w-[67.15px]" data-name="Input">
      <Container66 />
    </div>
  );
}

function Data25() {
  return (
    <div className="box-border content-stretch flex flex-col h-[59.956px] items-start justify-between pl-0 pr-[8.394px] py-[11.092px] relative shrink-0 w-[71.947px]" data-name="Data">
      <Input5 />
    </div>
  );
}

function Data26() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[74.345px]" data-name="Data">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="flex-none scale-y-[-100%]">
          <div className="bg-center bg-cover bg-no-repeat size-[20.385px]" data-name="image 5" style={{ backgroundImage: `url('${imgImage3}')` }} />
        </div>
      </div>
    </div>
  );
}

function Row2() {
  return (
    <div className="relative shrink-0 w-full" data-name="Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex gap-[16.788px] items-center justify-start pl-[8.394px] pr-0 py-0 relative w-full">
          <Data18 />
          <Data19 />
          <Data20 />
          <Data21 />
          <Data22 />
          <Data23 />
          <Data24 />
          <Data25 />
          <Data26 />
        </div>
      </div>
    </div>
  );
}

function Margin21() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start min-w-[125.907px] pl-0 pr-[8.394px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">삼성바이오로직스</p>
      </div>
    </div>
  );
}

function Border3() {
  return (
    <div className="relative rounded-[8.094px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[9.593px] py-[3.298px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.591px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[16.788px] whitespace-pre">207940</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8.094px]" />
    </div>
  );
}

function Data27() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-52" data-name="Data">
      <Margin21 />
      <Border3 />
    </div>
  );
}

function Container67() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">750,000</p>
      </div>
    </div>
  );
}

function Container68() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] w-full">
        <p className="leading-[20.984px]">789,000</p>
      </div>
    </div>
  );
}

function Data28() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[189px]" data-name="Data">
      <Container67 />
      <Container68 />
    </div>
  );
}

function Container69() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">2주</p>
      </div>
    </div>
  );
}

function Container70() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">1,578,000원</p>
      </div>
    </div>
  );
}

function Data29() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[187.769px]" data-name="Data">
      <Container69 />
      <Container70 />
    </div>
  );
}

function Frame12() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[12.591px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%_29.16%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector11} />
        </div>
      </div>
      <div className="absolute inset-[29.17%_8.33%_45.83%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector12} />
        </div>
      </div>
    </div>
  );
}

function Svg12() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[12.591px]" data-name="SVG">
      <Frame12 />
    </div>
  );
}

function SvgMargin10() {
  return (
    <div className="box-border content-stretch flex flex-col h-[12.591px] items-start justify-start pl-0 pr-[4.197px] py-0 relative shrink-0 w-[16.788px]" data-name="SVG:margin">
      <Svg12 />
    </div>
  );
}

function Container71() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">+5.2%</p>
      </div>
    </div>
  );
}

function Container72() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <SvgMargin10 />
      <Container71 />
    </div>
  );
}

function Container73() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.591px] w-full">
        <p className="leading-[16.788px]">(+78,000원)</p>
      </div>
    </div>
  );
}

function Data30() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.092px] pl-0 pr-[8.394px] pt-[9.893px] relative shrink-0 w-[158.882px]" data-name="Data">
      <Container72 />
      <Container73 />
    </div>
  );
}

function Data31() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[110px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">14.1%</p>
      </div>
    </div>
  );
}

function Container74() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start overflow-auto pl-0 pr-[5.24px] py-[1.199px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[normal] whitespace-pre">15</p>
      </div>
    </div>
  );
}

function Container75() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <Container74 />
    </div>
  );
}

function Container76() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pl-0 pr-[17.987px] py-0 relative shrink-0" data-name="Container">
      <Container75 />
    </div>
  );
}

function Input6() {
  return (
    <div className="bg-[#f3f3f5] box-border content-stretch flex h-[37.772px] items-start justify-start overflow-clip px-[13.79px] py-[10.492px] relative rounded-[8.094px] shrink-0 w-[67.15px]" data-name="Input">
      <Container76 />
    </div>
  );
}

function Data32() {
  return (
    <div className="box-border content-stretch flex flex-col h-[59.956px] items-start justify-between pl-0 pr-[8.394px] py-[11.092px] relative shrink-0 w-[71.947px]" data-name="Data">
      <Input6 />
    </div>
  );
}

function Data33() {
  return (
    <div className="box-border content-stretch flex flex-col items-center justify-center pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[95.653px]" data-name="Data">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">3</p>
      </div>
    </div>
  );
}

function Container77() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start overflow-auto pl-0 pr-[5.24px] py-[1.199px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[normal] whitespace-pre">5</p>
      </div>
    </div>
  );
}

function Container78() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <Container77 />
    </div>
  );
}

function Container79() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pl-0 pr-[17.987px] py-0 relative shrink-0" data-name="Container">
      <Container78 />
    </div>
  );
}

function Input7() {
  return (
    <div className="bg-[#f3f3f5] box-border content-stretch flex h-[37.772px] items-start justify-start overflow-clip px-[13.79px] py-[10.492px] relative rounded-[8.094px] shrink-0 w-[67.15px]" data-name="Input">
      <Container79 />
    </div>
  );
}

function Data34() {
  return (
    <div className="box-border content-stretch flex flex-col h-[59.956px] items-start justify-between pl-0 pr-[8.394px] py-[11.092px] relative shrink-0 w-[71.947px]" data-name="Data">
      <Input7 />
    </div>
  );
}

function Data35() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[74.345px]" data-name="Data">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="flex-none scale-y-[-100%]">
          <div className="bg-center bg-cover bg-no-repeat size-[20.385px]" data-name="image 6" style={{ backgroundImage: `url('${imgImage3}')` }} />
        </div>
      </div>
    </div>
  );
}

function Row3() {
  return (
    <div className="relative shrink-0 w-full" data-name="Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex gap-[16.788px] items-center justify-start pl-[8.394px] pr-0 py-0 relative w-full">
          <Data27 />
          <Data28 />
          <Data29 />
          <Data30 />
          <Data31 />
          <Data32 />
          <Data33 />
          <Data34 />
          <Data35 />
        </div>
      </div>
    </div>
  );
}

function Margin22() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[8.394px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">NAVER</p>
      </div>
    </div>
  );
}

function Border4() {
  return (
    <div className="relative rounded-[8.094px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[9.593px] py-[3.298px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.591px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[16.788px] whitespace-pre">035420</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8.094px]" />
    </div>
  );
}

function Data36() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[211px]" data-name="Data" style={{ gap: "8.52019e-15px" }}>
      <Margin22 />
      <Border4 />
    </div>
  );
}

function Container80() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">175,000</p>
      </div>
    </div>
  );
}

function Container81() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] w-full">
        <p className="leading-[20.984px]">183,500</p>
      </div>
    </div>
  );
}

function Data37() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[184px]" data-name="Data">
      <Container80 />
      <Container81 />
    </div>
  );
}

function Container82() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">25주</p>
      </div>
    </div>
  );
}

function Container83() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">4,587,500원</p>
      </div>
    </div>
  );
}

function Data38() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[187.769px]" data-name="Data">
      <Container82 />
      <Container83 />
    </div>
  );
}

function Frame13() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[12.591px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%_29.17%_8.34%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector11} />
        </div>
      </div>
      <div className="absolute inset-[29.17%_8.33%_45.83%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector12} />
        </div>
      </div>
    </div>
  );
}

function Svg13() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[12.591px]" data-name="SVG">
      <Frame13 />
    </div>
  );
}

function SvgMargin11() {
  return (
    <div className="box-border content-stretch flex flex-col h-[12.591px] items-start justify-start pl-0 pr-[4.197px] py-0 relative shrink-0 w-[16.788px]" data-name="SVG:margin">
      <Svg13 />
    </div>
  );
}

function Container84() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">+4.9%</p>
      </div>
    </div>
  );
}

function Container85() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <SvgMargin11 />
      <Container84 />
    </div>
  );
}

function Container86() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.591px] w-full">
        <p className="leading-[16.788px]">(+212,500원)</p>
      </div>
    </div>
  );
}

function Data39() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[10.492px] pl-0 pr-[8.394px] pt-[9.893px] relative shrink-0 w-[158.882px]" data-name="Data">
      <Container85 />
      <Container86 />
    </div>
  );
}

function Data40() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[19.486px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[115px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">11.4%</p>
      </div>
    </div>
  );
}

function Container87() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start overflow-auto pl-0 pr-[5.24px] py-[1.199px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[normal] whitespace-pre">10</p>
      </div>
    </div>
  );
}

function Container88() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <Container87 />
    </div>
  );
}

function Container89() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pl-0 pr-[17.987px] py-0 relative shrink-0" data-name="Container">
      <Container88 />
    </div>
  );
}

function Input8() {
  return (
    <div className="bg-[#f3f3f5] box-border content-stretch flex h-[37.772px] items-start justify-start overflow-clip px-[13.79px] py-[10.492px] relative rounded-[8.094px] shrink-0 w-[67.15px]" data-name="Input">
      <Container89 />
    </div>
  );
}

function Data41() {
  return (
    <div className="box-border content-stretch flex flex-col h-[59.956px] items-start justify-between pb-[10.492px] pl-0 pr-[8.394px] pt-[11.092px] relative shrink-0 w-[71.947px]" data-name="Data">
      <Input8 />
    </div>
  );
}

function Data42() {
  return (
    <div className="box-border content-stretch flex flex-col items-center justify-center pb-[19.486px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[95.653px]" data-name="Data">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-black text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">2</p>
      </div>
    </div>
  );
}

function Container90() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start overflow-auto pl-0 pr-[5.24px] py-[1.199px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[normal] whitespace-pre">3</p>
      </div>
    </div>
  );
}

function Container91() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <Container90 />
    </div>
  );
}

function Container92() {
  return (
    <div className="box-border content-stretch flex h-full items-center justify-start pl-0 pr-[17.987px] py-0 relative shrink-0" data-name="Container">
      <Container91 />
    </div>
  );
}

function Input9() {
  return (
    <div className="bg-[#f3f3f5] box-border content-stretch flex h-[37.772px] items-start justify-start overflow-clip px-[13.79px] py-[10.492px] relative rounded-[8.094px] shrink-0 w-[67.15px]" data-name="Input">
      <Container92 />
    </div>
  );
}

function Data43() {
  return (
    <div className="box-border content-stretch flex flex-col h-[59.956px] items-start justify-between pb-[10.492px] pl-0 pr-[8.394px] pt-[11.092px] relative shrink-0 w-[71.947px]" data-name="Data">
      <Input9 />
    </div>
  );
}

function Data44() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[19.486px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[95.653px]" data-name="Data">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="flex-none scale-y-[-100%]">
          <div className="bg-center bg-cover bg-no-repeat size-[20.385px]" data-name="image 6" style={{ backgroundImage: `url('${imgImage3}')` }} />
        </div>
      </div>
    </div>
  );
}

function Row4() {
  return (
    <div className="relative shrink-0 w-full" data-name="Row">
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex gap-[16.788px] items-center justify-start pl-[8.394px] pr-0 py-0 relative w-full">
          <Data36 />
          <Data37 />
          <Data38 />
          <Data39 />
          <Data40 />
          <Data41 />
          <Data42 />
          <Data43 />
          <Data44 />
        </div>
      </div>
    </div>
  );
}

function Body() {
  return (
    <div className="content-stretch flex flex-col h-[299.178px] items-start justify-between relative shrink-0 w-full" data-name="Body">
      <Row />
      <Row1 />
      <Row2 />
      <Row3 />
      <Row4 />
    </div>
  );
}

function Table() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start overflow-auto relative shrink-0 w-full z-[1]" data-name="Table">
      <HeaderRow />
      <Body />
    </div>
  );
}

function Container93() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col gap-[16.788px] isolate items-start justify-start pb-[25.181px] pt-0 px-[25.181px] relative w-full">
          <Table />
        </div>
      </div>
    </div>
  );
}

function BackgroundBorder() {
  return (
    <div className="bg-white box-border content-stretch flex flex-col gap-[25.181px] items-start justify-start p-[1.199px] relative rounded-[15.289px] shrink-0 w-[1535.43px]" data-name="Background+Border">
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[15.289px]" />
      <Container27 />
      <Container93 />
    </div>
  );
}

function Heading6() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 4">
      <div className="flex flex-col font-['Nico_Moji:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[23.982px] text-neutral-950 w-full">
        <p className="leading-[16.788px]">미등록 주식</p>
      </div>
    </div>
  );
}

function Container94() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col items-start justify-start pb-[6.295px] pt-[25.181px] px-[25.181px] relative w-full">
          <Heading6 />
        </div>
      </div>
    </div>
  );
}

function Cell9() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[227.831px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">종목명</p>
      </div>
    </div>
  );
}

function Cell10() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[201.451px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">매수가/현재가</p>
      </div>
    </div>
  );
}

function Cell11() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[204px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">수량/평가금액</p>
      </div>
    </div>
  );
}

function Cell12() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.392px] pt-[8.993px] px-[8.394px] relative shrink-0 w-[167.276px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">수익률</p>
      </div>
    </div>
  );
}

function HeaderRow1() {
  return (
    <div className="content-stretch flex items-start justify-start relative shrink-0 w-full" data-name="Header → Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <Cell9 />
      <Cell10 />
      <Cell11 />
      <Cell12 />
    </div>
  );
}

function Margin23() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start min-w-[67.15px] pl-0 pr-[8.394px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">삼성전자</p>
      </div>
    </div>
  );
}

function Border5() {
  return (
    <div className="relative rounded-[8.094px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[9.593px] py-[3.298px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.591px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[16.788px] whitespace-pre">005930</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8.094px]" />
    </div>
  );
}

function Data45() {
  return (
    <div className="content-stretch flex flex-col h-[38px] items-start justify-center relative shrink-0 w-[213px]" data-name="Data">
      <Margin23 />
      <Border5 />
    </div>
  );
}

function Container95() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">68,000</p>
      </div>
    </div>
  );
}

function Container96() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] w-full">
        <p className="leading-[20.984px]">71,800</p>
      </div>
    </div>
  );
}

function Data46() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[185px]" data-name="Data">
      <Container95 />
      <Container96 />
    </div>
  );
}

function Container97() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">50주</p>
      </div>
    </div>
  );
}

function Container98() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">3,590,000원</p>
      </div>
    </div>
  );
}

function Data47() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[187.769px]" data-name="Data">
      <Container97 />
      <Container98 />
    </div>
  );
}

function Frame14() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[12.591px]" data-name="Frame">
      <div className="absolute inset-[29.16%_8.33%_29.17%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector11} />
        </div>
      </div>
      <div className="absolute inset-[29.16%_8.33%_45.84%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector12} />
        </div>
      </div>
    </div>
  );
}

function Svg14() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[12.591px]" data-name="SVG">
      <Frame14 />
    </div>
  );
}

function SvgMargin12() {
  return (
    <div className="box-border content-stretch flex flex-col h-[12.591px] items-start justify-start pl-0 pr-[4.197px] py-0 relative shrink-0 w-[16.788px]" data-name="SVG:margin">
      <Svg14 />
    </div>
  );
}

function Container99() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">+5.6%</p>
      </div>
    </div>
  );
}

function Container100() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <SvgMargin12 />
      <Container99 />
    </div>
  );
}

function Container101() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.591px] w-full">
        <p className="leading-[16.788px]">(+190,000원)</p>
      </div>
    </div>
  );
}

function Data48() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.092px] pl-0 pr-[8.394px] pt-[9.893px] relative shrink-0 w-[158.882px]" data-name="Data">
      <Container100 />
      <Container101 />
    </div>
  );
}

function Data49() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[79.141px]" data-name="Data">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="flex-none scale-y-[-100%]">
          <div className="bg-center bg-cover bg-no-repeat size-[20.385px]" data-name="image 3" style={{ backgroundImage: `url('${imgImage3}')` }} />
        </div>
      </div>
    </div>
  );
}

function Row5() {
  return (
    <div className="relative shrink-0 w-full" data-name="Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex gap-[16.788px] items-center justify-start pl-[8.394px] pr-0 py-0 relative w-full">
          <Data45 />
          <Data46 />
          <Data47 />
          <Data48 />
          <Data49 />
        </div>
      </div>
    </div>
  );
}

function Margin24() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start min-w-[86.744px] pl-0 pr-[8.394px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">SK하이닉스</p>
      </div>
    </div>
  );
}

function Border6() {
  return (
    <div className="relative rounded-[8.094px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[9.593px] py-[3.298px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.591px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[16.788px] whitespace-pre">000660</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8.094px]" />
    </div>
  );
}

function Data50() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[214px]" data-name="Data">
      <Margin24 />
      <Border6 />
    </div>
  );
}

function Container102() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">85,000</p>
      </div>
    </div>
  );
}

function Container103() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] w-full">
        <p className="leading-[20.984px]">89,500</p>
      </div>
    </div>
  );
}

function Data51() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[183px]" data-name="Data">
      <Container102 />
      <Container103 />
    </div>
  );
}

function Container104() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">30주</p>
      </div>
    </div>
  );
}

function Container105() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">2,685,000원</p>
      </div>
    </div>
  );
}

function Data52() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[187.769px]" data-name="Data">
      <Container104 />
      <Container105 />
    </div>
  );
}

function Frame15() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[12.591px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%_29.16%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector11} />
        </div>
      </div>
      <div className="absolute inset-[29.17%_8.33%_45.83%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector12} />
        </div>
      </div>
    </div>
  );
}

function Svg15() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[12.591px]" data-name="SVG">
      <Frame15 />
    </div>
  );
}

function SvgMargin13() {
  return (
    <div className="box-border content-stretch flex flex-col h-[12.591px] items-start justify-start pl-0 pr-[4.197px] py-0 relative shrink-0 w-[16.788px]" data-name="SVG:margin">
      <Svg15 />
    </div>
  );
}

function Container106() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">+5.3%</p>
      </div>
    </div>
  );
}

function Container107() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <SvgMargin13 />
      <Container106 />
    </div>
  );
}

function Container108() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.591px] w-full">
        <p className="leading-[16.788px]">(+135,000원)</p>
      </div>
    </div>
  );
}

function Data53() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.092px] pl-0 pr-[8.394px] pt-[9.893px] relative shrink-0 w-[158.882px]" data-name="Data">
      <Container107 />
      <Container108 />
    </div>
  );
}

function Data54() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[93.531px]" data-name="Data">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="flex-none scale-y-[-100%]">
          <div className="bg-center bg-cover bg-no-repeat size-[20.385px]" data-name="image 4" style={{ backgroundImage: `url('${imgImage3}')` }} />
        </div>
      </div>
    </div>
  );
}

function Row6() {
  return (
    <div className="relative shrink-0 w-full" data-name="Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex gap-[16.788px] items-center justify-start pl-[8.394px] pr-0 py-0 relative w-full">
          <Data50 />
          <Data51 />
          <Data52 />
          <Data53 />
          <Data54 />
        </div>
      </div>
    </div>
  );
}

function Margin25() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start min-w-[115.51px] pl-0 pr-[8.394px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">LG에너지솔루션</p>
      </div>
    </div>
  );
}

function Border7() {
  return (
    <div className="relative rounded-[8.094px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[9.593px] py-[3.298px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.591px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[16.788px] whitespace-pre">373220</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8.094px]" />
    </div>
  );
}

function Data55() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[211px]" data-name="Data" style={{ gap: "1.70404e-14px" }}>
      <Margin25 />
      <Border7 />
    </div>
  );
}

function Container109() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">390,000</p>
      </div>
    </div>
  );
}

function Container110() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] w-full">
        <p className="leading-[20.984px]">412,000</p>
      </div>
    </div>
  );
}

function Data56() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[188px]" data-name="Data">
      <Container109 />
      <Container110 />
    </div>
  );
}

function Container111() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">15주</p>
      </div>
    </div>
  );
}

function Container112() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">6,180,000원</p>
      </div>
    </div>
  );
}

function Data57() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[187.769px]" data-name="Data">
      <Container111 />
      <Container112 />
    </div>
  );
}

function Frame16() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[12.591px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%_29.16%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector11} />
        </div>
      </div>
      <div className="absolute inset-[29.17%_8.33%_45.83%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector12} />
        </div>
      </div>
    </div>
  );
}

function Svg16() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[12.591px]" data-name="SVG">
      <Frame16 />
    </div>
  );
}

function SvgMargin14() {
  return (
    <div className="box-border content-stretch flex flex-col h-[12.591px] items-start justify-start pl-0 pr-[4.197px] py-0 relative shrink-0 w-[16.788px]" data-name="SVG:margin">
      <Svg16 />
    </div>
  );
}

function Container113() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">+5.6%</p>
      </div>
    </div>
  );
}

function Container114() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <SvgMargin14 />
      <Container113 />
    </div>
  );
}

function Container115() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.591px] w-full">
        <p className="leading-[16.788px]">(+330,000원)</p>
      </div>
    </div>
  );
}

function Data58() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.092px] pl-0 pr-[8.394px] pt-[9.893px] relative shrink-0 w-[158.882px]" data-name="Data">
      <Container114 />
      <Container115 />
    </div>
  );
}

function Data59() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[74.345px]" data-name="Data">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="flex-none scale-y-[-100%]">
          <div className="bg-center bg-cover bg-no-repeat size-[20.385px]" data-name="image 5" style={{ backgroundImage: `url('${imgImage3}')` }} />
        </div>
      </div>
    </div>
  );
}

function Row7() {
  return (
    <div className="relative shrink-0 w-full" data-name="Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex gap-[16.788px] items-center justify-start pl-[8.394px] pr-0 py-0 relative w-full">
          <Data55 />
          <Data56 />
          <Data57 />
          <Data58 />
          <Data59 />
        </div>
      </div>
    </div>
  );
}

function Margin26() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start min-w-[125.907px] pl-0 pr-[8.394px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">삼성바이오로직스</p>
      </div>
    </div>
  );
}

function Border8() {
  return (
    <div className="relative rounded-[8.094px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[9.593px] py-[3.298px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.591px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[16.788px] whitespace-pre">207940</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8.094px]" />
    </div>
  );
}

function Data60() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-52" data-name="Data">
      <Margin26 />
      <Border8 />
    </div>
  );
}

function Container116() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">750,000</p>
      </div>
    </div>
  );
}

function Container117() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] w-full">
        <p className="leading-[20.984px]">789,000</p>
      </div>
    </div>
  );
}

function Data61() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[189px]" data-name="Data">
      <Container116 />
      <Container117 />
    </div>
  );
}

function Container118() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">2주</p>
      </div>
    </div>
  );
}

function Container119() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">1,578,000원</p>
      </div>
    </div>
  );
}

function Data62() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[187.769px]" data-name="Data">
      <Container118 />
      <Container119 />
    </div>
  );
}

function Frame17() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[12.591px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%_29.16%_8.33%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector11} />
        </div>
      </div>
      <div className="absolute inset-[29.17%_8.33%_45.83%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector12} />
        </div>
      </div>
    </div>
  );
}

function Svg17() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[12.591px]" data-name="SVG">
      <Frame17 />
    </div>
  );
}

function SvgMargin15() {
  return (
    <div className="box-border content-stretch flex flex-col h-[12.591px] items-start justify-start pl-0 pr-[4.197px] py-0 relative shrink-0 w-[16.788px]" data-name="SVG:margin">
      <Svg17 />
    </div>
  );
}

function Container120() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">+5.2%</p>
      </div>
    </div>
  );
}

function Container121() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <SvgMargin15 />
      <Container120 />
    </div>
  );
}

function Container122() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.591px] w-full">
        <p className="leading-[16.788px]">(+78,000원)</p>
      </div>
    </div>
  );
}

function Data63() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[11.092px] pl-0 pr-[8.394px] pt-[9.893px] relative shrink-0 w-[158.882px]" data-name="Data">
      <Container121 />
      <Container122 />
    </div>
  );
}

function Data64() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[20.085px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[74.345px]" data-name="Data">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="flex-none scale-y-[-100%]">
          <div className="bg-center bg-cover bg-no-repeat size-[20.385px]" data-name="image 6" style={{ backgroundImage: `url('${imgImage3}')` }} />
        </div>
      </div>
    </div>
  );
}

function Row8() {
  return (
    <div className="relative shrink-0 w-full" data-name="Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex gap-[16.788px] items-center justify-start pl-[8.394px] pr-0 py-0 relative w-full">
          <Data60 />
          <Data61 />
          <Data62 />
          <Data63 />
          <Data64 />
        </div>
      </div>
    </div>
  );
}

function Margin27() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-[8.394px] py-0 relative shrink-0" data-name="Margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">NAVER</p>
      </div>
    </div>
  );
}

function Border9() {
  return (
    <div className="relative rounded-[8.094px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[9.593px] py-[3.298px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.591px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[16.788px] whitespace-pre">035420</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8.094px]" />
    </div>
  );
}

function Data65() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[211px]" data-name="Data" style={{ gap: "8.52019e-15px" }}>
      <Margin27 />
      <Border9 />
    </div>
  );
}

function Container123() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">175,000</p>
      </div>
    </div>
  );
}

function Container124() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] w-full">
        <p className="leading-[20.984px]">183,500</p>
      </div>
    </div>
  );
}

function Data66() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[184px]" data-name="Data">
      <Container123 />
      <Container124 />
    </div>
  );
}

function Container125() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">25주</p>
      </div>
    </div>
  );
}

function Container126() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1.199px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.749px] text-neutral-950 w-full">
        <p className="leading-[20.984px]">4,587,500원</p>
      </div>
    </div>
  );
}

function Data67() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[1.199px] pt-0 px-0 relative shrink-0 w-[187.769px]" data-name="Data">
      <Container125 />
      <Container126 />
    </div>
  );
}

function Frame21() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[12.591px]" data-name="Frame">
      <div className="absolute inset-[29.17%_8.33%_29.17%_8.34%]" data-name="Vector">
        <div className="absolute inset-[-10%_-5%]">
          <img className="block max-w-none size-full" src={imgVector11} />
        </div>
      </div>
      <div className="absolute inset-[29.17%_8.33%_45.83%_66.67%]" data-name="Vector">
        <div className="absolute inset-[-16.667%]">
          <img className="block max-w-none size-full" src={imgVector12} />
        </div>
      </div>
    </div>
  );
}

function Svg18() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[12.591px]" data-name="SVG">
      <Frame21 />
    </div>
  );
}

function SvgMargin16() {
  return (
    <div className="box-border content-stretch flex flex-col h-[12.591px] items-start justify-start pl-0 pr-[4.197px] py-0 relative shrink-0 w-[16.788px]" data-name="SVG:margin">
      <Svg18 />
    </div>
  );
}

function Container127() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[14.749px] text-nowrap">
        <p className="leading-[20.984px] whitespace-pre">+4.9%</p>
      </div>
    </div>
  );
}

function Container128() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <SvgMargin16 />
      <Container127 />
    </div>
  );
}

function Container129() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.591px] w-full">
        <p className="leading-[16.788px]">(+212,500원)</p>
      </div>
    </div>
  );
}

function Data68() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[10.492px] pl-0 pr-[8.394px] pt-[9.893px] relative shrink-0 w-[158.882px]" data-name="Data">
      <Container128 />
      <Container129 />
    </div>
  );
}

function Data69() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[19.486px] pl-0 pr-[8.394px] pt-[18.286px] relative shrink-0 w-[95.653px]" data-name="Data">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="flex-none scale-y-[-100%]">
          <div className="bg-center bg-cover bg-no-repeat size-[20.385px]" data-name="image 6" style={{ backgroundImage: `url('${imgImage3}')` }} />
        </div>
      </div>
    </div>
  );
}

function Row9() {
  return (
    <div className="relative shrink-0 w-full" data-name="Row">
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex gap-[16.788px] items-center justify-start pl-[8.394px] pr-0 py-0 relative w-full">
          <Data65 />
          <Data66 />
          <Data67 />
          <Data68 />
          <Data69 />
        </div>
      </div>
    </div>
  );
}

function Body1() {
  return (
    <div className="content-stretch flex flex-col h-[299.178px] items-start justify-between relative shrink-0 w-full" data-name="Body">
      <Row5 />
      <Row6 />
      <Row7 />
      <Row8 />
      <Row9 />
    </div>
  );
}

function Table1() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start overflow-auto relative shrink-0 w-full z-[1]" data-name="Table">
      <HeaderRow1 />
      <Body1 />
    </div>
  );
}

function Container130() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col gap-[16.788px] isolate items-start justify-start pb-[25.181px] pt-0 px-[25.181px] relative w-full">
          <Table1 />
        </div>
      </div>
    </div>
  );
}

function BackgroundBorder1() {
  return (
    <div className="bg-white box-border content-stretch flex flex-col gap-[25.181px] items-start justify-start p-[1.199px] relative rounded-[15.289px] shrink-0 w-[1535.43px]" data-name="Background+Border">
      <div aria-hidden="true" className="absolute border-[1.199px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[15.289px]" />
      <Container94 />
      <Container130 />
    </div>
  );
}

function Container131() {
  return (
    <div className="absolute content-stretch flex flex-col gap-[38px] inset-[1031.5px_248.13px_-261.56px_72px] items-start justify-start" data-name="Container">
      <BackgroundBorder />
      <BackgroundBorder1 />
    </div>
  );
}

function Svg19() {
  return <div className="absolute h-[427.656px] translate-x-[-50%] translate-y-[-50%] w-[1206.6px]" data-name="SVG" style={{ top: "calc(50% - 77.037px)", left: "calc(50% + 280.566px)" }} />;
}

function Heading5() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 5">
      <div className="flex flex-col font-['ABeeZee:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[21.563px] w-full">
        <p className="leading-[14.045px]">총 수익률</p>
      </div>
    </div>
  );
}

function Heading7() {
  return <div className="h-[15px] shrink-0 w-full" data-name="Heading 4" />;
}

function Container132() {
  return (
    <div className="absolute box-border content-stretch flex flex-col inset-[14.01%_64.69%_70.35%_2.36%] items-start justify-start pb-[9.832px] pt-[16.052px] px-[16.854px]" data-name="Container">
      <Heading5 />
      <div className="flex flex-col font-['ABeeZee:Regular',_sans-serif] h-[62px] justify-center leading-[0] not-italic relative shrink-0 text-[35.938px] text-neutral-950 w-full">
        <p className="leading-[22.472px]">%3.2%</p>
      </div>
      <div className="font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] not-italic relative shrink-0 text-[#0066cc] text-[25px] text-nowrap text-right">
        <p className="leading-[23.56px] whitespace-pre">+405,000원</p>
      </div>
      <Heading7 />
    </div>
  );
}

function Margin28() {
  return (
    <div className="box-border content-stretch flex flex-col h-[15.315px] items-start justify-start pl-0 pr-[10.21px] py-0 relative shrink-0 w-[25.526px]" data-name="Margin">
      <div className="bg-blue-500 rounded-[5.105px] shrink-0 size-[15.315px]" data-name="Background" />
    </div>
  );
}

function Container133() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start min-w-[72.267px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.941px] text-neutral-950 text-nowrap">
        <p className="leading-[25.526px] whitespace-pre">삼성전자</p>
      </div>
    </div>
  );
}

function Container134() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <Margin28 />
      <Container133 />
    </div>
  );
}

function Container135() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.941px] text-neutral-950 text-nowrap">
        <p className="leading-[25.526px] whitespace-pre">32.1%</p>
      </div>
    </div>
  );
}

function Container136() {
  return (
    <div className="content-stretch flex items-center justify-between relative shrink-0 w-full" data-name="Container">
      <Container134 />
      <Container135 />
    </div>
  );
}

function Margin29() {
  return (
    <div className="box-border content-stretch flex flex-col h-[15.315px] items-start justify-start pl-0 pr-[10.21px] py-0 relative shrink-0 w-[25.526px]" data-name="Margin">
      <div className="bg-emerald-500 rounded-[5.105px] shrink-0 size-[15.315px]" data-name="Background" />
    </div>
  );
}

function Container137() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start min-w-[96.366px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.941px] text-neutral-950 text-nowrap">
        <p className="leading-[25.526px] whitespace-pre">SK하이닉스</p>
      </div>
    </div>
  );
}

function Container138() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <Margin29 />
      <Container137 />
    </div>
  );
}

function Container139() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.941px] text-neutral-950 text-nowrap">
        <p className="leading-[25.526px] whitespace-pre">24.0%</p>
      </div>
    </div>
  );
}

function Container140() {
  return (
    <div className="content-stretch flex items-center justify-between relative shrink-0 w-full" data-name="Container">
      <Container138 />
      <Container139 />
    </div>
  );
}

function Margin30() {
  return (
    <div className="box-border content-stretch flex flex-col h-[15.315px] items-start justify-start pl-0 pr-[10.21px] py-0 relative shrink-0 w-[25.526px]" data-name="Margin">
      <div className="bg-amber-500 rounded-[5.105px] shrink-0 size-[15.315px]" data-name="Background" />
    </div>
  );
}

function Container141() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start min-w-[131.748px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.941px] text-neutral-950 text-nowrap">
        <p className="leading-[25.526px] whitespace-pre">LG에너지솔루션</p>
      </div>
    </div>
  );
}

function Container142() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <Margin30 />
      <Container141 />
    </div>
  );
}

function Container143() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.941px] text-neutral-950 text-nowrap">
        <p className="leading-[25.526px] whitespace-pre">18.5%</p>
      </div>
    </div>
  );
}

function Container144() {
  return (
    <div className="content-stretch flex items-center justify-between relative shrink-0 w-full" data-name="Container">
      <Container142 />
      <Container143 />
    </div>
  );
}

function Margin31() {
  return (
    <div className="box-border content-stretch flex flex-col h-[15.315px] items-start justify-start pl-0 pr-[10.21px] py-0 relative shrink-0 w-[25.526px]" data-name="Margin">
      <div className="bg-red-500 rounded-[5.105px] shrink-0 size-[15.315px]" data-name="Background" />
    </div>
  );
}

function Container145() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start min-w-[144.535px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.941px] text-neutral-950 text-nowrap">
        <p className="leading-[25.526px] whitespace-pre">삼성바이오로직스</p>
      </div>
    </div>
  );
}

function Container146() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <Margin31 />
      <Container145 />
    </div>
  );
}

function Container147() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.941px] text-neutral-950 text-nowrap">
        <p className="leading-[25.526px] whitespace-pre">14.1%</p>
      </div>
    </div>
  );
}

function Container148() {
  return (
    <div className="content-stretch flex items-center justify-between relative shrink-0 w-full" data-name="Container">
      <Container146 />
      <Container147 />
    </div>
  );
}

function Margin32() {
  return (
    <div className="box-border content-stretch flex flex-col h-[15.315px] items-start justify-start pl-0 pr-[10.21px] py-0 relative shrink-0 w-[25.526px]" data-name="Margin">
      <div className="bg-violet-500 rounded-[5.105px] shrink-0 size-[15.315px]" data-name="Background" />
    </div>
  );
}

function Container149() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.941px] text-neutral-950 text-nowrap">
        <p className="leading-[25.526px] whitespace-pre">NAVER</p>
      </div>
    </div>
  );
}

function Container150() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <Margin32 />
      <Container149 />
    </div>
  );
}

function Container151() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Consolas:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.941px] text-neutral-950 text-nowrap">
        <p className="leading-[25.526px] whitespace-pre">11.4%</p>
      </div>
    </div>
  );
}

function Container152() {
  return (
    <div className="content-stretch flex items-center justify-between relative shrink-0 w-full" data-name="Container">
      <Container150 />
      <Container151 />
    </div>
  );
}

function Container153() {
  return (
    <div className="content-stretch flex flex-col gap-[3.647px] items-start justify-start relative shrink-0 w-[292.051px]" data-name="Container">
      <Container136 />
      <Container140 />
      <Container144 />
      <Container148 />
      <Container152 />
    </div>
  );
}

function Container154() {
  return (
    <div className="absolute box-border content-stretch flex flex-col inset-[47.1%_75.71%_38.39%_2.88%] items-start justify-start pb-0 pt-[14.781px] px-[15.52px]" data-name="Container">
      <Container153 />
    </div>
  );
}

function Frame7() {
  return (
    <div className="absolute h-[66px] left-[30.8px] overflow-clip top-[18.31px] w-[232px]">
      <div className="absolute h-[30px] left-[0.27px] top-[12.69px] w-[75px]" data-name="Rectangle">
        <div aria-hidden="true" className="absolute border-[0px_0px_1.6px] border-neutral-950 border-solid inset-0 pointer-events-none" />
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[38.27px] not-italic text-[19px] text-center text-neutral-950 text-nowrap top-[10.69px] translate-x-[-50%]">
        <p className="leading-[20px] whitespace-pre">자산 현황</p>
      </div>
      <div className="absolute h-[30px] left-[95.27px] top-[12.69px] w-[93px]" data-name="Rectangle">
        <div aria-hidden="true" className="absolute border-[#4a5565] border-[0px_0px_1.6px] border-solid inset-0 pointer-events-none" />
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[141.77px] not-italic text-[#4a5565] text-[19px] text-center text-nowrap top-[10.69px] translate-x-[-50%]">
        <p className="leading-[20px] whitespace-pre">수익률 현황</p>
      </div>
    </div>
  );
}

function Frame22() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[17.725px]" data-name="Frame">
      <div className="absolute inset-[12.5%]" data-name="Vector">
        <div className="absolute inset-[-5.56%]">
          <img className="block max-w-none size-full" src={imgVector13} />
        </div>
      </div>
      <div className="absolute inset-[12.5%_66.67%_66.67%_12.5%]" data-name="Vector">
        <div className="absolute inset-[-20%]">
          <img className="block max-w-none size-full" src={imgVector14} />
        </div>
      </div>
      <div className="absolute bottom-[41.67%] left-1/2 right-[33.33%] top-[29.17%]" data-name="Vector">
        <div className="absolute inset-[-14.29%_-25.01%_-14.29%_-25%]">
          <img className="block max-w-none size-full" src={imgVector15} />
        </div>
      </div>
    </div>
  );
}

function Svg20() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[17.725px]" data-name="SVG">
      <Frame22 />
    </div>
  );
}

function SvgMargin17() {
  return (
    <div className="box-border content-stretch flex flex-col h-[17.725px] items-start justify-start pl-0 pr-[7.09px] py-0 relative shrink-0 w-[24.815px]" data-name="SVG:margin">
      <Svg20 />
    </div>
  );
}

function Container155() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start min-w-[117.4px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14.18px] text-neutral-950 text-nowrap">
        <p className="leading-[14.18px] whitespace-pre">리밸런싱 히스토리</p>
      </div>
    </div>
  );
}

function Heading8() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Heading 4">
      <SvgMargin17 />
      <Container155 />
    </div>
  );
}

function Container156() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col items-start justify-start pb-[5.317px] pt-[21.27px] px-[21.27px] relative w-full">
          <Heading8 />
        </div>
      </div>
    </div>
  );
}

function Cell13() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.622px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[209.721px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.458px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">실행 일시</p>
      </div>
    </div>
  );
}

function Cell14() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.622px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[139.541px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.458px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">유형</p>
      </div>
    </div>
  );
}

function Cell15() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.622px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[181.685px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.458px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">거래 종목</p>
      </div>
    </div>
  );
}

function Cell16() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.622px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[272.963px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.458px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">매수 금액</p>
      </div>
    </div>
  );
}

function Cell17() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.622px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[272.963px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.458px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">매도 금액</p>
      </div>
    </div>
  );
}

function Cell18() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.622px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[139.571px]" data-name="Cell">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.458px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">상태</p>
      </div>
    </div>
  );
}

function HeaderRow2() {
  return (
    <div className="content-stretch flex items-start justify-center relative shrink-0 w-full" data-name="Header → Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.013px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <Cell13 />
      <Cell14 />
      <Cell15 />
      <Cell16 />
      <Cell17 />
      <Cell18 />
    </div>
  );
}

function Data70() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.116px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[209.721px]" data-name="Data">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.263px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">2024-11-15</p>
      </div>
    </div>
  );
}

function Background2() {
  return (
    <div className="bg-[#eceef2] box-border content-stretch flex items-center justify-center overflow-clip px-[8.103px] py-[2.785px] relative rounded-[6.837px] shrink-0" data-name="Background">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#030213] text-[10.635px] text-center text-nowrap">
        <p className="leading-[14.18px] whitespace-pre">자동</p>
      </div>
    </div>
  );
}

function Data71() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start px-[7.09px] py-[7.596px] relative shrink-0 w-[139.541px]" data-name="Data">
      <Background2 />
    </div>
  );
}

function Data72() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.116px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[181.685px]" data-name="Data">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.458px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">3개</p>
      </div>
    </div>
  );
}

function Data73() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.116px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[272.963px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[12.458px] text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">+2,500,000원</p>
      </div>
    </div>
  );
}

function Data74() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.116px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[272.963px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.458px] text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">-1,800,000원</p>
      </div>
    </div>
  );
}

function Border10() {
  return (
    <div className="relative rounded-[6.837px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[8.103px] py-[2.785px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#00a63e] text-[10.635px] text-center text-nowrap">
          <p className="leading-[14.18px] whitespace-pre">성공</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.013px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[6.837px]" />
    </div>
  );
}

function Data75() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start px-[7.09px] py-[7.596px] relative shrink-0 w-[139.571px]" data-name="Data">
      <Border10 />
    </div>
  );
}

function Row10() {
  return (
    <div className="content-stretch flex items-start justify-center relative shrink-0 w-full" data-name="Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.013px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <Data70 />
      <Data71 />
      <Data72 />
      <Data73 />
      <Data74 />
      <Data75 />
    </div>
  );
}

function Data76() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.116px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[209.721px]" data-name="Data">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.263px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">2024-10-30</p>
      </div>
    </div>
  );
}

function Border11() {
  return (
    <div className="relative rounded-[6.837px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[8.103px] py-[2.785px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[10.635px] text-center text-neutral-950 text-nowrap">
          <p className="leading-[14.18px] whitespace-pre">수동</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.013px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[6.837px]" />
    </div>
  );
}

function Data77() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start px-[7.09px] py-[7.596px] relative shrink-0 w-[139.541px]" data-name="Data">
      <Border11 />
    </div>
  );
}

function Data78() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.116px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[181.685px]" data-name="Data">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.458px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">5개</p>
      </div>
    </div>
  );
}

function Data79() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.116px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[272.963px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[12.458px] text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">+3,200,000원</p>
      </div>
    </div>
  );
}

function Data80() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[9.116px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[272.963px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.458px] text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">-2,900,000원</p>
      </div>
    </div>
  );
}

function Border12() {
  return (
    <div className="relative rounded-[6.837px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[8.103px] py-[2.785px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#00a63e] text-[10.635px] text-center text-nowrap">
          <p className="leading-[14.18px] whitespace-pre">성공</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.013px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[6.837px]" />
    </div>
  );
}

function Data81() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start px-[7.09px] py-[7.596px] relative shrink-0 w-[139.571px]" data-name="Data">
      <Border12 />
    </div>
  );
}

function Row11() {
  return (
    <div className="content-stretch flex items-start justify-center relative shrink-0 w-full" data-name="Row">
      <div aria-hidden="true" className="absolute border-[0px_0px_1.013px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none" />
      <Data76 />
      <Data77 />
      <Data78 />
      <Data79 />
      <Data80 />
      <Data81 />
    </div>
  );
}

function Data82() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[8.609px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[209.721px]" data-name="Data">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.263px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">2024-10-15</p>
      </div>
    </div>
  );
}

function Background3() {
  return (
    <div className="bg-[#eceef2] box-border content-stretch flex items-center justify-center overflow-clip px-[8.103px] py-[2.785px] relative rounded-[6.837px] shrink-0" data-name="Background">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#030213] text-[10.635px] text-center text-nowrap">
        <p className="leading-[14.18px] whitespace-pre">자동</p>
      </div>
    </div>
  );
}

function Data83() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start p-[7.09px] relative shrink-0 w-[139.541px]" data-name="Data">
      <Background3 />
    </div>
  );
}

function Data84() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[8.609px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[181.685px]" data-name="Data">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.458px] text-neutral-950 text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">2개</p>
      </div>
    </div>
  );
}

function Data85() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[8.609px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[272.963px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[12.458px] text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">+1,200,000원</p>
      </div>
    </div>
  );
}

function Data86() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[8.609px] pt-[7.596px] px-[7.09px] relative shrink-0 w-[272.963px]" data-name="Data">
      <div className="flex flex-col font-['Consolas:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#e7000b] text-[12.458px] text-nowrap">
        <p className="leading-[17.725px] whitespace-pre">-800,000원</p>
      </div>
    </div>
  );
}

function Border13() {
  return (
    <div className="relative rounded-[6.837px] shrink-0" data-name="Border">
      <div className="box-border content-stretch flex items-center justify-center overflow-clip px-[8.103px] py-[2.785px] relative">
        <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#00a63e] text-[10.635px] text-center text-nowrap">
          <p className="leading-[14.18px] whitespace-pre">성공</p>
        </div>
      </div>
      <div aria-hidden="true" className="absolute border-[1.013px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[6.837px]" />
    </div>
  );
}

function Data87() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start p-[7.09px] relative shrink-0 w-[139.571px]" data-name="Data">
      <Border13 />
    </div>
  );
}

function Row12() {
  return (
    <div className="content-stretch flex items-start justify-center relative shrink-0 w-full" data-name="Row">
      <Data82 />
      <Data83 />
      <Data84 />
      <Data85 />
      <Data86 />
      <Data87 />
    </div>
  );
}

function Body2() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Body">
      <Row10 />
      <Row11 />
      <Row12 />
    </div>
  );
}

function Table2() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start overflow-auto relative shrink-0 w-full" data-name="Table">
      <HeaderRow2 />
      <Body2 />
    </div>
  );
}

function Container157() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col items-start justify-start pb-[21.27px] pt-0 px-[21.27px] relative w-full">
          <Table2 />
        </div>
      </div>
    </div>
  );
}

function BackgroundBorder2() {
  return (
    <div className="absolute bg-white box-border content-stretch flex flex-col gap-[21.27px] h-[199px] items-start justify-start left-[443px] p-[1.013px] right-[0.08px] rounded-[12.914px] top-[417.06px]" data-name="Background+Border">
      <div aria-hidden="true" className="absolute border-[1.013px] border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[12.914px]" />
      <Container156 />
      <Container157 />
    </div>
  );
}

function Frame23() {
  return (
    <div className="absolute h-[286px] left-[1106px] overflow-clip top-[86.06px] w-[392px]">
      <div className="absolute bg-[#fafbfc] h-[82px] left-[15px] rounded-[8px] top-2.5 w-[356px]" data-name="Rectangle">
        <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8px]" />
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-8 not-italic text-[18px] text-neutral-950 text-nowrap top-[27px]">
        <p className="leading-[26px] whitespace-pre">삼성전자</p>
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-8 not-italic text-[#4a5565] text-[12px] text-nowrap top-[57px]">
        <p className="leading-[18px] whitespace-pre">150주(1,200,000원) 매도</p>
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[360px] not-italic text-[#155dfc] text-[14px] text-nowrap text-right top-[26px] translate-x-[-100%]">
        <p className="leading-[18px] whitespace-pre">손익 +180,000원</p>
      </div>
      <div className="absolute font-['Inter:Regular',_sans-serif] font-normal leading-[0] left-[355px] not-italic text-[#155dfc] text-[12px] text-nowrap text-right top-[47px] translate-x-[-100%]">
        <p className="leading-[18px] whitespace-pre">+15.2%</p>
      </div>
      <div className="absolute bg-[#fafbfc] h-[82px] left-[15px] rounded-[8px] top-[104px] w-[356px]" data-name="Rectangle">
        <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8px]" />
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-8 not-italic text-[18px] text-neutral-950 text-nowrap top-[121px]">
        <p className="leading-[26px] whitespace-pre">LG전자</p>
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-8 not-italic text-[#4a5565] text-[12px] text-nowrap top-[151px]">
        <p className="leading-[18px] whitespace-pre">80주(800,000원) 매수</p>
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[361px] not-italic text-[#e7000b] text-[14px] text-nowrap text-right top-[120px] translate-x-[-100%]">
        <p className="leading-[18px] whitespace-pre">손익 -45,000원</p>
      </div>
      <div className="absolute font-['Inter:Regular',_sans-serif] font-normal leading-[0] left-[357px] not-italic text-[#e7000b] text-[12px] text-nowrap text-right top-[141px] translate-x-[-100%]">
        <p className="leading-[18px] whitespace-pre">-5.3%</p>
      </div>
      <div className="absolute bg-[#fafbfc] h-[82px] left-[15px] rounded-[8px] top-[198px] w-[356px]" data-name="Rectangle">
        <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[8px]" />
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-8 not-italic text-[18px] text-neutral-950 text-nowrap top-[215px]">
        <p className="leading-[26px] whitespace-pre">채권 ETF</p>
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-8 not-italic text-[#4a5565] text-[12px] text-nowrap top-[245px]">
        <p className="leading-[18px] whitespace-pre">200주(500,000원) 매수</p>
      </div>
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal leading-[0] left-[359px] not-italic text-[#155dfc] text-[14px] text-nowrap text-right top-[214px] translate-x-[-100%]">
        <p className="leading-[18px] whitespace-pre">손익 +25,000원</p>
      </div>
      <div className="absolute font-['Inter:Regular',_sans-serif] font-normal leading-[0] left-[353px] not-italic text-[#155dfc] text-[12px] text-nowrap text-right top-[235px] translate-x-[-100%]">
        <p className="leading-[18px] whitespace-pre">+5.1%</p>
      </div>
    </div>
  );
}

function Group18() {
  return (
    <div className="absolute contents left-[1096px] top-[76.06px]">
      <div className="absolute bg-white h-[306px] left-[1096px] rounded-[18px] top-[76.06px] w-[400px]" data-name="Rectangle">
        <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.3)] border-solid inset-0 pointer-events-none rounded-[18px]" />
      </div>
      <Frame23 />
    </div>
  );
}

function Component1() {
  return (
    <div className="absolute bg-white h-[660.352px] left-[59px] right-[66.92px] rounded-[18.416px] top-[329.5px]" data-name="포트폴리오그래프">
      <div aria-hidden="true" className="absolute border-[1.444px] border-[rgba(0,0,0,0.3)] border-solid inset-0 pointer-events-none rounded-[18.416px]" />
      <Svg19 />
      <Container132 />
      <div className="absolute flex flex-col font-['ABeeZee:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[14.045px] left-[54px] not-italic text-[#4a5565] text-[21.563px] top-[276.56px] translate-y-[-50%] w-[481.995px]">
        <p className="mb-0 text-black">평가 자산 : 180,620,000</p>
        <p className="mb-0">&nbsp;</p>
        <p>&nbsp;</p>
      </div>
      <Container154 />
      <Frame7 />
      <div className="absolute bg-center bg-cover bg-no-repeat h-[379px] left-[460px] top-[51.06px] w-[602px]" data-name="image 22" style={{ backgroundImage: `url('${imgImage22}')` }} />
      <BackgroundBorder2 />
      <Group18 />
    </div>
  );
}

function Container158() {
  return (
    <div className="bg-white h-[2140.98px] relative shrink-0 w-full" data-name="Container">
      <Container26 />
      <Component />
      <Container131 />
      <Component1 />
    </div>
  );
}

function Main() {
  return (
    <div className="bg-white content-stretch flex flex-col h-[2141px] items-start justify-start relative shrink-0 w-[1691px]" data-name="Main">
      <Container158 />
    </div>
  );
}

export default function Component2() {
  return (
    <div className="bg-white content-stretch flex flex-col items-center justify-center relative size-full" data-name="대시보드-수익률 현황">
      <Header />
      <Nav />
      <BackgroundHorizontalBorder />
      <Main />
    </div>
  );
}