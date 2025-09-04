import imgReBalancePro from "figma:asset/060eddf81c61ee29d1847760635f03574a9494ff.png";
import img from "figma:asset/654e0924cc8319b8a3e662daabd00ddbff610480.png";
import img1 from "figma:asset/d52bccf7f987fc9b0bda0846c0141355d7823ae1.png";
import { imgVector, imgVector1, imgVector2, imgVector3, imgSvg, imgSvg1, imgSvg2, imgSvg3, imgSvg4, imgSvg5, imgSvg6, imgImage, imgVector4, imgVector5, imgVector6, imgVector7, imgVector8, imgVector9 } from "./svg-25rg9";

function Frame() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[10.5px]" data-name="Frame">
      <div className="absolute inset-[8.32%_12.49%]" data-name="Vector">
        <div className="absolute inset-[-5%_-5.55%]">
          <img className="block max-w-none size-full" src={imgVector} />
        </div>
      </div>
    </div>
  );
}

function Svg() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-[10.5px]" data-name="SVG">
      <Frame />
    </div>
  );
}

function SvgMargin() {
  return (
    <div className="box-border content-stretch flex flex-col h-[10.5px] items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0 w-3.5" data-name="SVG:margin">
      <Svg />
    </div>
  );
}

function Background() {
  return (
    <div className="bg-blue-50 box-border content-stretch flex gap-[3.5px] items-center justify-center overflow-clip px-2 py-[2.75px] relative rounded-[6.75px] shrink-0" data-name="Background">
      <SvgMargin />
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[10.5px] text-center text-nowrap">
        <p className="leading-[14px] whitespace-pre">AI 자동 리밸런싱</p>
      </div>
    </div>
  );
}

function Heading1() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 1">
      <div className="bg-clip-text bg-gradient-to-r flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] from-[#101828] justify-center leading-[65.63px] not-italic relative shrink-0 text-[52.5px] text-nowrap to-[#59168b] via-50% via-[#1c398e] whitespace-pre" style={{ WebkitTextFillColor: "transparent" }}>
        <p className="mb-0">투자의 미래,</p>
        <p>스마트한 선택</p>
      </div>
    </div>
  );
}

function Container() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-0 pt-[1.2px] px-0 relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[28.44px] not-italic relative shrink-0 text-[#4a5565] text-[17.5px] w-full">
        <p className="mb-0">복잡한 포트폴리오 관리를 AI가 대신합니다.</p>
        <p>자동 리밸런싱으로 더 안전하고 수익성 높은 투자를 시작하세요.</p>
      </div>
    </div>
  );
}

function Container1() {
  return (
    <div className="content-stretch flex flex-col gap-[12.8px] items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Background />
      <Heading1 />
      <Container />
    </div>
  );
}

function Frame1() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-1/2 left-[20.83%] right-[20.83%] top-1/2" data-name="Vector">
        <div className="absolute inset-[-0.58px_-7.14%]">
          <img className="block max-w-none size-full" src={imgVector1} />
        </div>
      </div>
      <div className="absolute bottom-[20.83%] left-1/2 right-[20.83%] top-[20.83%]" data-name="Vector">
        <div className="absolute inset-[-7.14%_-14.29%]">
          <img className="block max-w-none size-full" src={imgVector2} />
        </div>
      </div>
    </div>
  );
}

function Svg1() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame1 />
    </div>
  );
}

function SvgMargin1() {
  return (
    <div className="absolute box-border content-stretch flex flex-col h-3.5 items-start justify-start left-[110.16px] pl-[7px] pr-0 py-0 top-[10.5px] w-[21px]" data-name="SVG:margin">
      <Svg1 />
    </div>
  );
}

function Button() {
  return (
    <div className="bg-gradient-to-r from-[#155dfc] h-[35px] relative rounded-[6.75px] shrink-0 to-[#9810fa] w-[145.16px]" data-name="Button">
      <div className="absolute flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] h-[18px] justify-center leading-[0] not-italic text-[12.3px] text-center text-white translate-x-[-50%] translate-y-[-50%] w-[89.36px]" style={{ top: "calc(50% - 0.75px)", left: "calc(50% - 13.9px)" }}>
        <p className="leading-[17.5px]">무료로 시작하기</p>
      </div>
      <SvgMargin1 />
    </div>
  );
}

function Frame2() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[12.5%] left-1/4 right-[16.67%] top-[12.5%]" data-name="Vector">
        <div className="absolute inset-[-5.56%_-7.14%]">
          <img className="block max-w-none size-full" src={imgVector3} />
        </div>
      </div>
    </div>
  );
}

function Svg2() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame2 />
    </div>
  );
}

function SvgMargin2() {
  return (
    <div className="absolute box-border content-stretch flex flex-col h-3.5 items-start justify-start left-[15px] pl-0 pr-[7px] py-0 top-[10.5px] w-[21px]" data-name="SVG:margin">
      <Svg2 />
    </div>
  );
}

function Button1() {
  return (
    <div className="bg-white h-[35px] relative rounded-[6.75px] shrink-0 w-[138.31px]" data-name="Button">
      <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[6.75px]" />
      <SvgMargin2 />
      <div className="absolute flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] h-[18px] justify-center leading-[0] not-italic text-[12.3px] text-center text-neutral-950 translate-x-[-50%] translate-y-[-50%] w-[80.51px]" style={{ top: "calc(50% - 0.75px)", left: "calc(50% + 14.1px)" }}>
        <p className="leading-[17.5px]">데모 영상 보기</p>
      </div>
    </div>
  );
}

function Container2() {
  return (
    <div className="content-stretch flex gap-3.5 items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Button />
      <Button1 />
    </div>
  );
}

function Container3() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#101828] text-[21px] text-center w-full">
        <p className="leading-[28px]">10만+</p>
      </div>
    </div>
  );
}

function Container4() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.3px] text-center w-full">
        <p className="leading-[17.5px]">누적 사용자</p>
      </div>
    </div>
  );
}

function Container5() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-[2.5px] grow items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Container3 />
      <Container4 />
    </div>
  );
}

function Container6() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] relative shrink-0 text-[#101828] text-[21px] text-center w-full" style={{ fontVariationSettings: "'CTGR' 0, 'wdth' 100, 'wght' 400" }}>
        <p className="leading-[28px]">₩2,847억</p>
      </div>
    </div>
  );
}

function Container7() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.3px] text-center w-full">
        <p className="leading-[17.5px]">관리 자산</p>
      </div>
    </div>
  );
}

function Container8() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-[2.5px] grow items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Container6 />
      <Container7 />
    </div>
  );
}

function Container9() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#101828] text-[21px] text-center w-full">
        <p className="leading-[28px]">18.5%</p>
      </div>
    </div>
  );
}

function Container10() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.3px] text-center w-full">
        <p className="leading-[17.5px]">평균 수익률</p>
      </div>
    </div>
  );
}

function Container11() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-[2.5px] grow items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Container9 />
      <Container10 />
    </div>
  );
}

function Container12() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#101828] text-[21px] text-center w-full">
        <p className="leading-[28px]">99.9%</p>
      </div>
    </div>
  );
}

function Container13() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.3px] text-center w-full">
        <p className="leading-[17.5px]">시스템 안정성</p>
      </div>
    </div>
  );
}

function Container14() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-[2.5px] grow items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Container12 />
      <Container13 />
    </div>
  );
}

function Container15() {
  return (
    <div className="box-border content-stretch flex gap-7 items-start justify-center pb-0 pt-7 px-0 relative shrink-0 w-full" data-name="Container">
      <Container5 />
      <Container8 />
      <Container11 />
      <Container14 />
    </div>
  );
}

function Container16() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-7 grow items-start justify-start min-h-px min-w-px relative shrink-0" data-name="Container">
      <Container1 />
      <Container2 />
      <Container15 />
    </div>
  );
}

function ReBalancePro() {
  return <div className="bg-no-repeat bg-size-[100%_100%] bg-top-left h-[336px] max-w-[448px] rounded-[14px] shadow-[0px_25px_50px_-12px_rgba(0,0,0,0.25)] shrink-0 w-full" data-name="ReBalance Pro 모바일 앱" style={{ backgroundImage: `url('${imgReBalancePro}')` }} />;
}

function Container17() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col items-start justify-start px-[45.5px] py-0 relative w-full">
          <div className="absolute blur-[32px] filter inset-[-16.8px_-26.95px] rounded-[14px]" data-name="Gradient+Blur" />
          <ReBalancePro />
        </div>
      </div>
    </div>
  );
}

function Section() {
  return (
    <div className="absolute content-stretch flex gap-[42px] items-center justify-center left-[400px] right-[400px] top-[127px]" data-name="Section">
      <Container16 />
      <Container17 />
    </div>
  );
}

function Heading2() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 2">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[31.5px] text-center text-neutral-950 w-full">
        <p className="leading-[35px]">왜 ReBalance Pro인가요?</p>
      </div>
    </div>
  );
}

function Container18() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start max-w-[588px] relative shrink-0 w-[588px]" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[17.5px] text-center text-nowrap">
        <p className="leading-[24.5px] whitespace-pre">복잡한 투자 관리를 간단하게 만들어주는 스마트한 기능들</p>
      </div>
    </div>
  );
}

function Container19() {
  return (
    <div className="content-stretch flex flex-col gap-3.5 items-center justify-start relative shrink-0 w-full" data-name="Container">
      <Heading2 />
      <Container18 />
    </div>
  );
}

function Svg3() {
  return (
    <div className="relative shrink-0 size-[21px]" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg} />
    </div>
  );
}

function Background1() {
  return (
    <div className="absolute bg-gradient-to-r content-stretch flex from-[#155dfc] items-center justify-center left-7 rounded-[8.75px] size-[42px] to-[#9810fa] top-7" data-name="Background">
      <Svg3 />
    </div>
  );
}

function Heading3() {
  return (
    <div className="absolute content-stretch flex flex-col items-start justify-start left-7 right-7 top-[91px]" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-neutral-950 text-nowrap">
        <p className="leading-[24.5px] whitespace-pre">자동 리밸런싱</p>
      </div>
    </div>
  );
}

function Container20() {
  return (
    <div className="absolute content-stretch flex flex-col items-start justify-start left-7 right-7 top-[125.25px]" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[22.75px] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap whitespace-pre">
        <p className="mb-0">목표 비중을 설정하면 AI가 자</p>
        <p className="mb-0">동으로 포트폴리오를 균형있</p>
        <p>게 조정합니다.</p>
      </div>
    </div>
  );
}

function Container21() {
  return (
    <div className="h-[215.25px] relative shrink-0 w-full" data-name="Container">
      <Background1 />
      <Heading3 />
      <Container20 />
    </div>
  );
}

function BackgroundShadow() {
  return (
    <div className="basis-0 bg-white box-border content-stretch flex flex-col grow items-start justify-start min-h-px min-w-px overflow-clip relative rounded-[12.75px] self-stretch shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1),0px_4px_6px_-4px_rgba(0,0,0,0.1)] shrink-0" data-name="Background+Shadow">
      <Container21 />
    </div>
  );
}

function Svg4() {
  return (
    <div className="relative shrink-0 size-[21px]" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg1} />
    </div>
  );
}

function Background2() {
  return (
    <div className="absolute bg-gradient-to-r content-stretch flex from-[#155dfc] items-center justify-center left-7 rounded-[8.75px] size-[42px] to-[#9810fa] top-7" data-name="Background">
      <Svg4 />
    </div>
  );
}

function Heading5() {
  return (
    <div className="absolute content-stretch flex flex-col items-start justify-start left-7 right-7 top-[91px]" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-neutral-950 text-nowrap">
        <p className="leading-[24.5px] whitespace-pre">실시간 분석</p>
      </div>
    </div>
  );
}

function Container22() {
  return (
    <div className="absolute content-stretch flex flex-col items-start justify-start left-7 right-7 top-[125.25px]" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[22.75px] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap whitespace-pre">
        <p className="mb-0">실시간 시장 데이터와 포트폴</p>
        <p className="mb-0">리오 성과를 한눈에 확인하세</p>
        <p>요.</p>
      </div>
    </div>
  );
}

function Container23() {
  return (
    <div className="h-[215.25px] relative shrink-0 w-full" data-name="Container">
      <Background2 />
      <Heading5 />
      <Container22 />
    </div>
  );
}

function BackgroundShadow1() {
  return (
    <div className="basis-0 bg-white box-border content-stretch flex flex-col grow items-start justify-start min-h-px min-w-px overflow-clip relative rounded-[12.75px] self-stretch shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1),0px_4px_6px_-4px_rgba(0,0,0,0.1)] shrink-0" data-name="Background+Shadow">
      <Container23 />
    </div>
  );
}

function Svg5() {
  return (
    <div className="relative shrink-0 size-[21px]" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg2} />
    </div>
  );
}

function Background3() {
  return (
    <div className="absolute bg-gradient-to-r content-stretch flex from-[#155dfc] items-center justify-center left-7 rounded-[8.75px] size-[42px] to-[#9810fa] top-7" data-name="Background">
      <Svg5 />
    </div>
  );
}

function Heading6() {
  return (
    <div className="absolute content-stretch flex flex-col items-start justify-start left-7 right-7 top-[91px]" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-neutral-950 text-nowrap">
        <p className="leading-[24.5px] whitespace-pre">안전한 투자</p>
      </div>
    </div>
  );
}

function Container24() {
  return (
    <div className="absolute content-stretch flex flex-col items-start justify-start left-7 right-7 top-[125.25px]" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[22.75px] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap whitespace-pre">
        <p className="mb-0">리스크 관리와 분산투자로 더</p>
        <p className="mb-0">안전한 자산 운용이 가능합니</p>
        <p>다.</p>
      </div>
    </div>
  );
}

function Container25() {
  return (
    <div className="h-[215.25px] relative shrink-0 w-full" data-name="Container">
      <Background3 />
      <Heading6 />
      <Container24 />
    </div>
  );
}

function BackgroundShadow2() {
  return (
    <div className="basis-0 bg-white box-border content-stretch flex flex-col grow items-start justify-start min-h-px min-w-px overflow-clip relative rounded-[12.75px] self-stretch shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1),0px_4px_6px_-4px_rgba(0,0,0,0.1)] shrink-0" data-name="Background+Shadow">
      <Container25 />
    </div>
  );
}

function Svg6() {
  return (
    <div className="relative shrink-0 size-[21px]" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg3} />
    </div>
  );
}

function Background4() {
  return (
    <div className="absolute bg-gradient-to-r content-stretch flex from-[#155dfc] items-center justify-center left-7 rounded-[8.75px] size-[42px] to-[#9810fa] top-7" data-name="Background">
      <Svg6 />
    </div>
  );
}

function Heading7() {
  return (
    <div className="absolute content-stretch flex flex-col items-start justify-start left-7 right-7 top-[91px]" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-neutral-950 text-nowrap">
        <p className="leading-[24.5px] whitespace-pre">빠른 실행</p>
      </div>
    </div>
  );
}

function Container26() {
  return (
    <div className="absolute content-stretch flex flex-col items-start justify-start left-7 right-7 top-[125.25px]" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[22.75px] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap whitespace-pre">
        <p className="mb-0">클릭 한 번으로 리밸런싱을 실</p>
        <p className="mb-0">행하고 최적의 포트폴리오를</p>
        <p>유지하세요.</p>
      </div>
    </div>
  );
}

function Container27() {
  return (
    <div className="h-[215.25px] relative shrink-0 w-full" data-name="Container">
      <Background4 />
      <Heading7 />
      <Container26 />
    </div>
  );
}

function BackgroundShadow3() {
  return (
    <div className="basis-0 bg-white box-border content-stretch flex flex-col grow items-start justify-start min-h-px min-w-px overflow-clip relative rounded-[12.75px] self-stretch shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1),0px_4px_6px_-4px_rgba(0,0,0,0.1)] shrink-0" data-name="Background+Shadow">
      <Container27 />
    </div>
  );
}

function Container28() {
  return (
    <div className="content-stretch flex gap-7 items-start justify-center relative shrink-0 w-full" data-name="Container">
      <BackgroundShadow />
      <BackgroundShadow1 />
      <BackgroundShadow2 />
      <BackgroundShadow3 />
    </div>
  );
}

function Container29() {
  return (
    <div className="max-w-[1120px] relative shrink-0 w-full" data-name="Container">
      <div className="max-w-inherit relative size-full">
        <div className="box-border content-stretch flex flex-col gap-14 items-start justify-start max-w-inherit px-7 py-0 relative w-full">
          <Container19 />
          <Container28 />
        </div>
      </div>
    </div>
  );
}

function Section1() {
  return (
    <div className="absolute bg-gray-50 box-border content-stretch flex flex-col items-start justify-start left-0 px-[400px] py-[70px] right-0 top-[586.63px]" data-name="Section">
      <Container29 />
    </div>
  );
}

function Heading8() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 2">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[31.5px] text-center text-neutral-950 w-full">
        <p className="leading-[35px]">3단계로 시작하는 스마트 투자</p>
      </div>
    </div>
  );
}

function Container30() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[17.5px] text-center w-full">
        <p className="leading-[24.5px]">복잡한 설정 없이 바로 시작할 수 있어요</p>
      </div>
    </div>
  );
}

function Container31() {
  return (
    <div className="content-stretch flex flex-col gap-3.5 items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Heading8 />
      <Container30 />
    </div>
  );
}

function Svg7() {
  return (
    <div className="relative shrink-0 size-7" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg4} />
    </div>
  );
}

function Background5() {
  return (
    <div className="bg-gradient-to-r content-stretch flex from-[#155dfc] items-center justify-center relative rounded-[3.35544e+07px] shrink-0 size-[70px] to-[#9810fa]" data-name="Background">
      <Svg7 />
    </div>
  );
}

function Container32() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[12.3px] text-center text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">01</p>
      </div>
    </div>
  );
}

function BackgroundBorder() {
  return (
    <div className="absolute bg-white box-border content-stretch flex items-center justify-center pb-[5.25px] pt-[4.25px] px-0.5 right-[-7px] rounded-[3.35544e+07px] size-7 top-[-7px]" data-name="Background+Border">
      <div aria-hidden="true" className="absolute border-2 border-[#155dfc] border-solid inset-0 pointer-events-none rounded-[3.35544e+07px]" />
      <Container32 />
    </div>
  );
}

function Container33() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full z-[2]" data-name="Container">
      <Background5 />
      <BackgroundBorder />
    </div>
  );
}

function Heading9() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-center text-neutral-950 w-full">
        <p className="leading-[24.5px]">포트폴리오 설정</p>
      </div>
    </div>
  );
}

function Container34() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-center w-full">
        <p className="leading-[22.75px]">보유 주식과 목표 비중을 간단하게 입력하세요.</p>
      </div>
    </div>
  );
}

function Container35() {
  return (
    <div className="content-stretch flex flex-col gap-[10.5px] items-start justify-start relative shrink-0 w-full z-[1]" data-name="Container">
      <Heading9 />
      <Container34 />
    </div>
  );
}

function Container36() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-[21px] grow isolate items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Container33 />
      <Container35 />
    </div>
  );
}

function Svg8() {
  return (
    <div className="relative shrink-0 size-7" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg5} />
    </div>
  );
}

function Background6() {
  return (
    <div className="bg-gradient-to-r content-stretch flex from-[#155dfc] items-center justify-center relative rounded-[3.35544e+07px] shrink-0 size-[70px] to-[#9810fa]" data-name="Background">
      <Svg8 />
    </div>
  );
}

function Container37() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[12.3px] text-center text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">02</p>
      </div>
    </div>
  );
}

function BackgroundBorder1() {
  return (
    <div className="absolute bg-white box-border content-stretch flex items-center justify-center pb-[5.25px] pt-[4.25px] px-0.5 right-[-7px] rounded-[3.35544e+07px] size-7 top-[-7px]" data-name="Background+Border">
      <div aria-hidden="true" className="absolute border-2 border-[#155dfc] border-solid inset-0 pointer-events-none rounded-[3.35544e+07px]" />
      <Container37 />
    </div>
  );
}

function Container38() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full z-[2]" data-name="Container">
      <Background6 />
      <BackgroundBorder1 />
    </div>
  );
}

function Heading10() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-center text-neutral-950 w-full">
        <p className="leading-[24.5px]">AI 분석</p>
      </div>
    </div>
  );
}

function Container39() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[22.75px] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-center w-full">
        <p className="mb-0">AI가 시장 상황과 포트폴리오 균형을 실시간으로 분석</p>
        <p>합니다.</p>
      </div>
    </div>
  );
}

function Container40() {
  return (
    <div className="content-stretch flex flex-col gap-[9.875px] items-start justify-start relative shrink-0 w-full z-[1]" data-name="Container">
      <Heading10 />
      <Container39 />
    </div>
  );
}

function Container41() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-[21px] grow isolate items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Container38 />
      <Container40 />
    </div>
  );
}

function Svg9() {
  return (
    <div className="relative shrink-0 size-7" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg6} />
    </div>
  );
}

function Background7() {
  return (
    <div className="bg-gradient-to-r content-stretch flex from-[#155dfc] items-center justify-center relative rounded-[3.35544e+07px] shrink-0 size-[70px] to-[#9810fa]" data-name="Background">
      <Svg9 />
    </div>
  );
}

function Container42() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#155dfc] text-[12.3px] text-center text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">03</p>
      </div>
    </div>
  );
}

function BackgroundBorder2() {
  return (
    <div className="absolute bg-white box-border content-stretch flex items-center justify-center pb-[5.25px] pt-[4.25px] px-0.5 right-[-7px] rounded-[3.35544e+07px] size-7 top-[-7px]" data-name="Background+Border">
      <div aria-hidden="true" className="absolute border-2 border-[#155dfc] border-solid inset-0 pointer-events-none rounded-[3.35544e+07px]" />
      <Container42 />
    </div>
  );
}

function Container43() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full z-[2]" data-name="Container">
      <Background7 />
      <BackgroundBorder2 />
    </div>
  );
}

function Heading11() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-center text-neutral-950 w-full">
        <p className="leading-[24.5px]">자동 실행</p>
      </div>
    </div>
  );
}

function Container44() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-center w-full">
        <p className="leading-[22.75px]">최적의 타이밍에 자동으로 리밸런싱이 실행됩니다.</p>
      </div>
    </div>
  );
}

function Container45() {
  return (
    <div className="content-stretch flex flex-col gap-[10.5px] items-start justify-start relative shrink-0 w-full z-[1]" data-name="Container">
      <Heading11 />
      <Container44 />
    </div>
  );
}

function Container46() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-[21px] grow isolate items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Container43 />
      <Container45 />
    </div>
  );
}

function Container47() {
  return (
    <div className="content-stretch flex gap-7 items-start justify-center relative shrink-0 w-full" data-name="Container">
      <Container36 />
      <Container41 />
      <Container46 />
    </div>
  );
}

function Section2() {
  return (
    <div className="absolute box-border content-stretch flex flex-col gap-14 items-start justify-start left-[400px] max-w-[1120px] px-7 py-0 right-[400px] top-[1141.38px]" data-name="Section">
      <Container31 />
      <Container47 />
    </div>
  );
}

function Heading12() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 2">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[31.5px] text-center text-neutral-950 w-full">
        <p className="leading-[35px]">고객들의 이야기</p>
      </div>
    </div>
  );
}

function Container48() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[17.5px] text-center w-full">
        <p className="leading-[24.5px]">실제 사용자들의 생생한 후기를 확인해보세요</p>
      </div>
    </div>
  );
}

function Container49() {
  return (
    <div className="content-stretch flex flex-col gap-3.5 items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Heading12 />
      <Container48 />
    </div>
  );
}

function Margin() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0 w-[17.5px]" data-name="Margin">
      <div className="bg-[#fdc700] rounded-[3.35544e+07px] shrink-0 size-3.5" data-name="Background" />
    </div>
  );
}

function Container50() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      {[...Array(4).keys()].map((_, i) => (
        <Margin key={i} />
      ))}
      <div className="bg-[#fdc700] rounded-[3.35544e+07px] shrink-0 size-3.5" data-name="Background" />
    </div>
  );
}

function Container51() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[22.75px] not-italic relative shrink-0 text-[#4a5565] text-[14px] w-full">
        <p className="mb-0">{`"복잡한 리밸런싱을 자동으로 해주니까 정말`}</p>
        <p>{`편해요. 수익률도 20% 이상 올랐습니다!"`}</p>
      </div>
    </div>
  );
}

function Component() {
  return <div className="bg-no-repeat bg-size-[100%_100%] bg-top-left max-w-[280px] rounded-[3.35544e+07px] shrink-0 size-[35px]" data-name="김민수" style={{ backgroundImage: `url('${img}')` }} />;
}

function ImgMargin() {
  return (
    <div className="box-border content-stretch flex flex-col h-[35px] items-start justify-start max-w-[290.5px] pl-0 pr-[10.5px] py-0 relative shrink-0 w-[45.5px]" data-name="Img - 김민수:margin">
      <Component />
    </div>
  );
}

function Container52() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-neutral-950 text-nowrap">
        <p className="leading-[21px] whitespace-pre">김민수</p>
      </div>
    </div>
  );
}

function Container53() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">직장인 투자자</p>
      </div>
    </div>
  );
}

function Container54() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-px pt-0 px-0 relative shrink-0" data-name="Container">
      <Container52 />
      <Container53 />
    </div>
  );
}

function Container55() {
  return (
    <div className="box-border content-stretch flex items-center justify-start pb-0 pt-[7.6px] px-0 relative shrink-0 w-full" data-name="Container">
      <ImgMargin />
      <Container54 />
    </div>
  );
}

function Container56() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col gap-[13.4px] items-start justify-start pb-[21px] pt-7 px-7 relative w-full">
          <Container50 />
          <Container51 />
          <Container55 />
        </div>
      </div>
    </div>
  );
}

function BackgroundShadow4() {
  return (
    <div className="basis-0 bg-white box-border content-stretch flex flex-col grow items-start justify-start min-h-px min-w-px overflow-clip relative rounded-[12.75px] self-stretch shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1),0px_4px_6px_-4px_rgba(0,0,0,0.1)] shrink-0" data-name="Background+Shadow">
      <Container56 />
    </div>
  );
}

function Margin4() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0 w-[17.5px]" data-name="Margin">
      <div className="bg-[#fdc700] rounded-[3.35544e+07px] shrink-0 size-3.5" data-name="Background" />
    </div>
  );
}

function Container57() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      {[...Array(4).keys()].map((_, i) => (
        <Margin4 key={i} />
      ))}
      <div className="bg-[#fdc700] rounded-[3.35544e+07px] shrink-0 size-3.5" data-name="Background" />
    </div>
  );
}

function Container58() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[22.75px] not-italic relative shrink-0 text-[#4a5565] text-[14px] w-full">
        <p className="mb-0">{`"백테스트 기능으로 전략을 검증하고 실제 적`}</p>
        <p>{`용했더니 안정적인 수익을 얻고 있어요."`}</p>
      </div>
    </div>
  );
}

function Image() {
  return (
    <div className="absolute left-1/2 size-[88px] top-1/2 translate-x-[-50%] translate-y-[-50%]" data-name="image">
      <img className="block max-w-none size-full" src={imgImage} />
    </div>
  );
}

function ImageFill() {
  return (
    <div className="overflow-clip relative shrink-0 size-[35px]" data-name="image fill">
      <Image />
    </div>
  );
}

function ErrorLoadingImage() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start max-w-[35px] overflow-clip relative shrink-0" data-name="Error loading image">
      <ImageFill />
    </div>
  );
}

function Container59() {
  return (
    <div className="basis-0 content-stretch flex grow items-center justify-center min-h-px min-w-px relative shrink-0 w-full" data-name="Container">
      <ErrorLoadingImage />
    </div>
  );
}

function Background8() {
  return (
    <div className="bg-gray-100 content-stretch flex flex-col items-start justify-center relative rounded-[3.35544e+07px] shrink-0 size-[35px]" data-name="Background">
      <Container59 />
    </div>
  );
}

function Margin8() {
  return (
    <div className="box-border content-stretch flex flex-col h-[35px] items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[45.5px]" data-name="Margin">
      <Background8 />
    </div>
  );
}

function Container60() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-neutral-950 text-nowrap">
        <p className="leading-[21px] whitespace-pre">박지영</p>
      </div>
    </div>
  );
}

function Container61() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">전업 투자자</p>
      </div>
    </div>
  );
}

function Container62() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-px pt-0 px-0 relative shrink-0" data-name="Container">
      <Container60 />
      <Container61 />
    </div>
  );
}

function Container63() {
  return (
    <div className="box-border content-stretch flex items-center justify-start pb-0 pt-[7.6px] px-0 relative shrink-0 w-full" data-name="Container">
      <Margin8 />
      <Container62 />
    </div>
  );
}

function Container64() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col gap-[13.4px] items-start justify-start pb-[21px] pt-7 px-7 relative w-full">
          <Container57 />
          <Container58 />
          <Container63 />
        </div>
      </div>
    </div>
  );
}

function BackgroundShadow5() {
  return (
    <div className="basis-0 bg-white box-border content-stretch flex flex-col grow items-start justify-start min-h-px min-w-px overflow-clip relative rounded-[12.75px] self-stretch shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1),0px_4px_6px_-4px_rgba(0,0,0,0.1)] shrink-0" data-name="Background+Shadow">
      <Container64 />
    </div>
  );
}

function Margin9() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[3.5px] py-0 relative shrink-0 w-[17.5px]" data-name="Margin">
      <div className="bg-[#fdc700] rounded-[3.35544e+07px] shrink-0 size-3.5" data-name="Background" />
    </div>
  );
}

function Container65() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      {[...Array(4).keys()].map((_, i) => (
        <Margin9 key={i} />
      ))}
      <div className="bg-[#fdc700] rounded-[3.35544e+07px] shrink-0 size-3.5" data-name="Background" />
    </div>
  );
}

function Container66() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[22.75px] not-italic relative shrink-0 text-[#4a5565] text-[14px] w-full">
        <p className="mb-0">{`"투자 초보자도 쉽게 사용할 수 있어서 좋아`}</p>
        <p className="mb-0">요. 이제 전문가처럼 포트폴리오를 관리해</p>
        <p>{`요."`}</p>
      </div>
    </div>
  );
}

function Component1() {
  return <div className="bg-no-repeat bg-size-[100%_100%] bg-top-left max-w-[280px] rounded-[3.35544e+07px] shrink-0 size-[35px]" data-name="이동현" style={{ backgroundImage: `url('${img1}')` }} />;
}

function ImgMargin1() {
  return (
    <div className="box-border content-stretch flex flex-col h-[35px] items-start justify-start max-w-[290.5px] pl-0 pr-[10.5px] py-0 relative shrink-0 w-[45.5px]" data-name="Img - 이동현:margin">
      <Component1 />
    </div>
  );
}

function Container67() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-neutral-950 text-nowrap">
        <p className="leading-[21px] whitespace-pre">이동현</p>
      </div>
    </div>
  );
}

function Container68() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start mb-[-1px] relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[12.3px] text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">신규 투자자</p>
      </div>
    </div>
  );
}

function Container69() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-px pt-0 px-0 relative shrink-0" data-name="Container">
      <Container67 />
      <Container68 />
    </div>
  );
}

function Container70() {
  return (
    <div className="box-border content-stretch flex items-center justify-start pb-0 pt-[7.7px] px-0 relative shrink-0 w-full" data-name="Container">
      <ImgMargin1 />
      <Container69 />
    </div>
  );
}

function Container71() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col gap-[13.3px] items-start justify-start pb-[21px] pt-7 px-7 relative w-full">
          <Container65 />
          <Container66 />
          <Container70 />
        </div>
      </div>
    </div>
  );
}

function BackgroundShadow6() {
  return (
    <div className="basis-0 bg-white box-border content-stretch flex flex-col grow items-start justify-start min-h-px min-w-px overflow-clip relative rounded-[12.75px] self-stretch shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1),0px_4px_6px_-4px_rgba(0,0,0,0.1)] shrink-0" data-name="Background+Shadow">
      <Container71 />
    </div>
  );
}

function Container72() {
  return (
    <div className="content-stretch flex gap-7 items-start justify-center relative shrink-0 w-full" data-name="Container">
      <BackgroundShadow4 />
      <BackgroundShadow5 />
      <BackgroundShadow6 />
    </div>
  );
}

function Container73() {
  return (
    <div className="max-w-[1120px] relative shrink-0 w-full" data-name="Container">
      <div className="max-w-inherit relative size-full">
        <div className="box-border content-stretch flex flex-col gap-14 items-start justify-start max-w-inherit px-7 py-0 relative w-full">
          <Container49 />
          <Container72 />
        </div>
      </div>
    </div>
  );
}

function Section3() {
  return (
    <div className="absolute bg-gray-50 box-border content-stretch flex flex-col items-start justify-start left-0 px-[400px] py-[70px] right-0 top-[1512.38px]" data-name="Section">
      <Container73 />
    </div>
  );
}

function Heading13() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 2">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[31.5px] text-center text-neutral-950 w-full">
        <p className="leading-[35px]">투자 목표에 맞는 요금제</p>
      </div>
    </div>
  );
}

function Container74() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[17.5px] text-center w-full">
        <p className="leading-[24.5px]">언제든지 업그레이드나 다운그레이드가 가능해요</p>
      </div>
    </div>
  );
}

function Container75() {
  return (
    <div className="content-stretch flex flex-col gap-3.5 items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Heading13 />
      <Container74 />
    </div>
  );
}

function Heading14() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-center text-neutral-950 w-full">
        <p className="leading-[24.5px]">Basic</p>
      </div>
    </div>
  );
}

function Container76() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[26.3px] text-center text-neutral-950 w-full">
        <p className="leading-[31.5px]">무료</p>
      </div>
    </div>
  );
}

function Container77() {
  return (
    <div className="box-border content-stretch flex flex-col items-center justify-start pb-0 pt-px px-0 relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-center w-full">
        <p className="leading-[21px]">개인 투자자를 위한 기본 기능</p>
      </div>
    </div>
  );
}

function Container78() {
  return (
    <div className="content-stretch flex flex-col gap-[13px] items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Heading14 />
      <Container76 />
      <Container77 />
    </div>
  );
}

function Frame3() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
    </div>
  );
}

function Svg10() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame3 />
    </div>
  );
}

function SvgMargin3() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[24.5px]" data-name="SVG:margin">
      <Svg10 />
    </div>
  );
}

function Container79() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">5개 종목 포트폴리오</p>
      </div>
    </div>
  );
}

function Item() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin3 />
      <Container79 />
    </div>
  );
}

function Frame4() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
    </div>
  );
}

function Svg11() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame4 />
    </div>
  );
}

function SvgMargin4() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[24.5px]" data-name="SVG:margin">
      <Svg11 />
    </div>
  );
}

function Container80() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">월 1회 자동 리밸런싱</p>
      </div>
    </div>
  );
}

function Item1() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin4 />
      <Container80 />
    </div>
  );
}

function Frame5() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
    </div>
  );
}

function Svg12() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame5 />
    </div>
  );
}

function SvgMargin5() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[24.5px]" data-name="SVG:margin">
      <Svg12 />
    </div>
  );
}

function Container81() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">기본 성과 분석</p>
      </div>
    </div>
  );
}

function Item2() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin5 />
      <Container81 />
    </div>
  );
}

function Frame6() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
    </div>
  );
}

function Svg13() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame6 />
    </div>
  );
}

function SvgMargin6() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[24.5px]" data-name="SVG:margin">
      <Svg13 />
    </div>
  );
}

function Container82() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">이메일 지원</p>
      </div>
    </div>
  );
}

function Item3() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin6 />
      <Container82 />
    </div>
  );
}

function List() {
  return (
    <div className="content-stretch flex flex-col gap-[10.5px] items-start justify-start relative shrink-0 w-full" data-name="List">
      <Item />
      <Item1 />
      <Item2 />
      <Item3 />
    </div>
  );
}

function Button2() {
  return (
    <div className="bg-white h-[31.5px] relative rounded-[6.75px] shrink-0 w-full" data-name="Button">
      <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[6.75px]" />
      <div className="flex flex-row items-center justify-center relative size-full">
        <div className="box-border content-stretch flex h-[31.5px] items-center justify-center pb-[7.5px] pt-1.5 px-[15px] relative w-full">
          <div className="basis-0 flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] grow justify-center leading-[0] min-h-px min-w-px not-italic relative shrink-0 text-[12.3px] text-center text-neutral-950">
            <p className="leading-[17.5px]">시작하기</p>
          </div>
        </div>
      </div>
    </div>
  );
}

function Container83() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col gap-7 items-start justify-start pb-[21px] pt-7 px-7 relative w-full">
          <Container78 />
          <List />
          <Button2 />
        </div>
      </div>
    </div>
  );
}

function BackgroundBorder3() {
  return (
    <div className="bg-white box-border content-stretch flex flex-col h-full items-start justify-start p-[2px] relative rounded-[12.75px] shrink-0 w-[336px]" data-name="Background+Border">
      <div aria-hidden="true" className="absolute border-2 border-gray-200 border-solid inset-0 pointer-events-none rounded-[12.75px]" />
      <Container83 />
    </div>
  );
}

function Heading15() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[18.375px] text-center text-neutral-950 w-full">
        <p className="leading-[25.725px]">Pro</p>
      </div>
    </div>
  );
}

function Container84() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] max-h-[33.075px] overflow-ellipsis overflow-hidden relative shrink-0 text-[27.615px] text-center text-neutral-950 w-full" style={{ fontVariationSettings: "'CTGR' 0, 'wdth' 100, 'wght' 400" }}>
        <p className="leading-[33px]">₩29,000/월</p>
      </div>
    </div>
  );
}

function Container85() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] max-h-[22.05px] not-italic overflow-ellipsis overflow-hidden relative shrink-0 text-[#4a5565] text-[14.7px] text-center w-full">
        <p className="leading-[22px]">전문 투자자를 위한 고급 기능</p>
      </div>
    </div>
  );
}

function Container86() {
  return (
    <div className="content-stretch flex flex-col gap-[13.5px] items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Heading15 />
      <Container84 />
      <Container85 />
    </div>
  );
}

function Frame7() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[14.7px]" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector5} />
        </div>
      </div>
    </div>
  );
}

function Svg14() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[14.7px]" data-name="SVG">
      <Frame7 />
    </div>
  );
}

function SvgMargin7() {
  return (
    <div className="box-border content-stretch flex flex-col h-[14.7px] items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[25.2px]" data-name="SVG:margin">
      <Svg14 />
    </div>
  );
}

function Container87() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start min-w-[155.18px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] max-h-[22.05px] not-italic overflow-ellipsis overflow-hidden relative shrink-0 text-[#4a5565] text-[14.7px] text-nowrap">
        <p className="leading-[22px] whitespace-pre">무제한 종목 포트폴리오</p>
      </div>
    </div>
  );
}

function Item4() {
  return (
    <div className="content-stretch flex gap-[0.53px] items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin7 />
      <Container87 />
    </div>
  );
}

function Frame8() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[14.7px]" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector5} />
        </div>
      </div>
    </div>
  );
}

function Svg15() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[14.7px]" data-name="SVG">
      <Frame8 />
    </div>
  );
}

function SvgMargin8() {
  return (
    <div className="box-border content-stretch flex flex-col h-[14.7px] items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[25.2px]" data-name="SVG:margin">
      <Svg15 />
    </div>
  );
}

function Container88() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start min-w-[140.48px] relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] max-h-[22.05px] not-italic overflow-ellipsis overflow-hidden relative shrink-0 text-[#4a5565] text-[14.7px] text-nowrap">
        <p className="leading-[22px] whitespace-pre">실시간 자동 리밸런싱</p>
      </div>
    </div>
  );
}

function Item5() {
  return (
    <div className="content-stretch flex gap-[0.53px] items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin8 />
      <Container88 />
    </div>
  );
}

function Frame9() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[14.7px]" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector5} />
        </div>
      </div>
    </div>
  );
}

function Svg16() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[14.7px]" data-name="SVG">
      <Frame9 />
    </div>
  );
}

function SvgMargin9() {
  return (
    <div className="box-border content-stretch flex flex-col h-[14.7px] items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[25.2px]" data-name="SVG:margin">
      <Svg16 />
    </div>
  );
}

function Container89() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] max-h-[22.05px] not-italic overflow-ellipsis overflow-hidden relative shrink-0 text-[#4a5565] text-[14.7px] text-nowrap">
        <p className="leading-[22px] whitespace-pre">고급 백테스트 분석</p>
      </div>
    </div>
  );
}

function Item6() {
  return (
    <div className="content-stretch flex gap-[0.53px] items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin9 />
      <Container89 />
    </div>
  );
}

function Frame10() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[14.7px]" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector5} />
        </div>
      </div>
    </div>
  );
}

function Svg17() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[14.7px]" data-name="SVG">
      <Frame10 />
    </div>
  );
}

function SvgMargin10() {
  return (
    <div className="box-border content-stretch flex flex-col h-[14.7px] items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[25.2px]" data-name="SVG:margin">
      <Svg17 />
    </div>
  );
}

function Container90() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] max-h-[22.05px] not-italic overflow-ellipsis overflow-hidden relative shrink-0 text-[#4a5565] text-[14.7px] text-nowrap">
        <p className="leading-[22px] whitespace-pre">AI 투자 추천</p>
      </div>
    </div>
  );
}

function Item7() {
  return (
    <div className="content-stretch flex gap-[0.53px] items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin10 />
      <Container90 />
    </div>
  );
}

function Frame11() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[14.7px]" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector5} />
        </div>
      </div>
    </div>
  );
}

function Svg18() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[14.7px]" data-name="SVG">
      <Frame11 />
    </div>
  );
}

function SvgMargin11() {
  return (
    <div className="box-border content-stretch flex flex-col h-[14.7px] items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[25.2px]" data-name="SVG:margin">
      <Svg18 />
    </div>
  );
}

function Container91() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] max-h-[22.05px] not-italic overflow-ellipsis overflow-hidden relative shrink-0 text-[#4a5565] text-[14.7px] text-nowrap">
        <p className="leading-[22px] whitespace-pre">우선 고객 지원</p>
      </div>
    </div>
  );
}

function Item8() {
  return (
    <div className="content-stretch flex gap-[0.53px] items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin11 />
      <Container91 />
    </div>
  );
}

function Frame12() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[14.7px]" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector5} />
        </div>
      </div>
    </div>
  );
}

function Svg19() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-[14.7px]" data-name="SVG">
      <Frame12 />
    </div>
  );
}

function SvgMargin12() {
  return (
    <div className="box-border content-stretch flex flex-col h-[14.7px] items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[25.2px]" data-name="SVG:margin">
      <Svg19 />
    </div>
  );
}

function Container92() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] max-h-[22.05px] not-italic overflow-ellipsis overflow-hidden relative shrink-0 text-[#4a5565] text-[14.7px] text-nowrap">
        <p className="leading-[22px] whitespace-pre">API 연동</p>
      </div>
    </div>
  );
}

function Item9() {
  return (
    <div className="content-stretch flex gap-[0.53px] items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin12 />
      <Container92 />
    </div>
  );
}

function List1() {
  return (
    <div className="content-stretch flex flex-col gap-2.5 items-start justify-start relative shrink-0 w-full" data-name="List">
      <Item4 />
      <Item5 />
      <Item6 />
      <Item7 />
      <Item8 />
      <Item9 />
    </div>
  );
}

function Button3() {
  return (
    <div className="bg-gradient-to-r from-[#155dfc] h-[34.07px] relative rounded-[6.75px] shrink-0 to-[#9810fa] w-full" data-name="Button">
      <div className="flex flex-row items-center justify-center relative size-full">
        <div className="box-border content-stretch flex h-[34.07px] items-center justify-center pb-[7.82px] pt-[7.25px] px-3.5 relative w-full">
          <div className="basis-0 flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] grow justify-center leading-[0] min-h-px min-w-px not-italic relative shrink-0 text-[12.915px] text-center text-white">
            <p className="leading-[18.375px]">시작하기</p>
          </div>
        </div>
      </div>
    </div>
  );
}

function Container93() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col gap-[28.4px] items-start justify-start pb-[22.06px] pt-[29.39px] px-[29.4px] relative w-full">
          <Container86 />
          <List1 />
          <Button3 />
        </div>
      </div>
    </div>
  );
}

function Frame13() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-[11.02px]" data-name="Frame">
      <div className="absolute inset-[53.71%_29.17%_8.34%_29.18%]" data-name="Vector">
        <div className="absolute inset-[-10.98%_-10%]">
          <img className="block max-w-none size-full" src={imgVector6} />
        </div>
      </div>
      <div className="absolute bottom-[41.67%] left-1/4 right-1/4 top-[8.33%]" data-name="Vector">
        <div className="absolute inset-[-8.333%]">
          <img className="block max-w-none size-full" src={imgVector7} />
        </div>
      </div>
    </div>
  );
}

function Svg20() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-[11.02px]" data-name="SVG">
      <Frame13 />
    </div>
  );
}

function SvgMargin13() {
  return (
    <div className="absolute box-border content-stretch flex flex-col h-[11.02px] items-start justify-start left-[15.75px] pl-0 pr-[3.5px] py-0 top-[6.56px] w-[14.52px]" data-name="SVG:margin">
      <Svg20 />
    </div>
  );
}

function Background9() {
  return (
    <div className="bg-gradient-to-r from-[#155dfc] h-[24.14px] overflow-clip relative rounded-[6.75px] shrink-0 to-[#9810fa] w-[71.92px]" data-name="Background">
      <SvgMargin13 />
      <div className="absolute flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] h-[15px] justify-center leading-[0] not-italic text-[11.025px] text-center text-white translate-x-[-50%] translate-y-[-50%] w-[22.25px]" style={{ top: "calc(50% - 0.525px)", left: "calc(50% + 9.285px)" }}>
        <p className="leading-[14.7px]">추천</p>
      </div>
    </div>
  );
}

function Container94() {
  return (
    <div className="absolute content-stretch flex flex-col items-start justify-start left-[39.81%] right-[39.81%] top-[-12.59px]" data-name="Container">
      <Background9 />
    </div>
  );
}

function BackgroundBorderShadow() {
  return (
    <div className="bg-white h-[445.2px] relative rounded-[12.75px] shrink-0 w-[352.8px]" data-name="Background+Border+Shadow">
      <div className="box-border content-stretch flex flex-col h-[445.2px] items-start justify-start overflow-clip p-[2px] relative w-[352.8px]">
        <Container93 />
        <Container94 />
      </div>
      <div aria-hidden="true" className="absolute border-2 border-[#155dfc] border-solid inset-0 pointer-events-none rounded-[12.75px] shadow-[0px_20px_25px_-5px_rgba(0,0,0,0.1),0px_8px_10px_-6px_rgba(0,0,0,0.1)]" />
    </div>
  );
}

function Heading16() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-center text-neutral-950 w-full">
        <p className="leading-[24.5px]">Enterprise</p>
      </div>
    </div>
  );
}

function Container95() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[26.3px] text-center text-neutral-950 w-full">
        <p className="leading-[31.5px]">문의</p>
      </div>
    </div>
  );
}

function Container96() {
  return (
    <div className="box-border content-stretch flex flex-col items-center justify-start pb-0 pt-px px-0 relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-center w-full">
        <p className="leading-[21px]">기관 투자자를 위한 엔터프라이즈</p>
      </div>
    </div>
  );
}

function Container97() {
  return (
    <div className="content-stretch flex flex-col gap-[13px] items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Heading16 />
      <Container95 />
      <Container96 />
    </div>
  );
}

function Frame14() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
    </div>
  );
}

function Svg21() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame14 />
    </div>
  );
}

function SvgMargin14() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[24.5px]" data-name="SVG:margin">
      <Svg21 />
    </div>
  );
}

function Container98() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">모든 Pro 기능</p>
      </div>
    </div>
  );
}

function Item10() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin14 />
      <Container98 />
    </div>
  );
}

function Frame15() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
    </div>
  );
}

function Svg22() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame15 />
    </div>
  );
}

function SvgMargin15() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[24.5px]" data-name="SVG:margin">
      <Svg22 />
    </div>
  );
}

function Container99() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">전담 어드바이저</p>
      </div>
    </div>
  );
}

function Item11() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin15 />
      <Container99 />
    </div>
  );
}

function Frame16() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
    </div>
  );
}

function Svg23() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame16 />
    </div>
  );
}

function SvgMargin16() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[24.5px]" data-name="SVG:margin">
      <Svg23 />
    </div>
  );
}

function Container100() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">커스텀 전략 개발</p>
      </div>
    </div>
  );
}

function Item12() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin16 />
      <Container100 />
    </div>
  );
}

function Frame17() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
    </div>
  );
}

function Svg24() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame17 />
    </div>
  );
}

function SvgMargin17() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[24.5px]" data-name="SVG:margin">
      <Svg24 />
    </div>
  );
}

function Container101() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">온프레미스 설치</p>
      </div>
    </div>
  );
}

function Item13() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin17 />
      <Container101 />
    </div>
  );
}

function Frame18() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[29.17%] left-[16.67%] right-[16.67%] top-1/4" data-name="Vector">
        <div className="absolute inset-[-9.09%_-6.25%]">
          <img className="block max-w-none size-full" src={imgVector4} />
        </div>
      </div>
    </div>
  );
}

function Svg25() {
  return (
    <div className="content-stretch flex flex-col items-start justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame18 />
    </div>
  );
}

function SvgMargin18() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[24.5px]" data-name="SVG:margin">
      <Svg25 />
    </div>
  );
}

function Container102() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">SLA 보장</p>
      </div>
    </div>
  );
}

function Item14() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Item">
      <SvgMargin18 />
      <Container102 />
    </div>
  );
}

function List2() {
  return (
    <div className="content-stretch flex flex-col gap-[10.5px] items-start justify-start relative shrink-0 w-full" data-name="List">
      <Item10 />
      <Item11 />
      <Item12 />
      <Item13 />
      <Item14 />
    </div>
  );
}

function Button4() {
  return (
    <div className="bg-white h-[31.5px] relative rounded-[6.75px] shrink-0 w-full" data-name="Button">
      <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[6.75px]" />
      <div className="flex flex-row items-center justify-center relative size-full">
        <div className="box-border content-stretch flex h-[31.5px] items-center justify-center pb-[7.5px] pt-1.5 px-[15px] relative w-full">
          <div className="basis-0 flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] grow justify-center leading-[0] min-h-px min-w-px not-italic relative shrink-0 text-[12.3px] text-center text-neutral-950">
            <p className="leading-[17.5px]">문의하기</p>
          </div>
        </div>
      </div>
    </div>
  );
}

function Container103() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col gap-7 items-start justify-start pb-[21px] pt-7 px-7 relative w-full">
          <Container97 />
          <List2 />
          <Button4 />
        </div>
      </div>
    </div>
  );
}

function BackgroundBorder4() {
  return (
    <div className="bg-white box-border content-stretch flex flex-col h-full items-start justify-start p-[2px] relative rounded-[12.75px] shrink-0 w-[336px]" data-name="Background+Border">
      <div aria-hidden="true" className="absolute border-2 border-gray-200 border-solid inset-0 pointer-events-none rounded-[12.75px]" />
      <Container103 />
    </div>
  );
}

function Container104() {
  return (
    <div className="content-stretch flex gap-[19.6px] h-[424px] items-center justify-center relative shrink-0 w-full" data-name="Container">
      <BackgroundBorder3 />
      <BackgroundBorderShadow />
      <BackgroundBorder4 />
    </div>
  );
}

function Section4() {
  return (
    <div className="absolute box-border content-stretch flex flex-col gap-14 items-start justify-start left-[400px] max-w-[1120px] px-7 py-0 right-[400px] top-[2056.63px]" data-name="Section">
      <Container75 />
      <Container104 />
    </div>
  );
}

function Heading17() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 2">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[31.5px] text-center text-white w-full">
        <p className="leading-[35px]">더 스마트한 투자, 지금 시작하세요</p>
      </div>
    </div>
  );
}

function Container105() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start opacity-90 relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-center text-white w-full">
        <p className="leading-[24.5px]">14일 무료 체험으로 ReBalance Pro의 모든 기능을 경험해보세요</p>
      </div>
    </div>
  );
}

function Frame19() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-1/2 left-[20.83%] right-[20.83%] top-1/2" data-name="Vector">
        <div className="absolute inset-[-0.58px_-7.14%]">
          <img className="block max-w-none size-full" src={imgVector8} />
        </div>
      </div>
      <div className="absolute bottom-[20.83%] left-1/2 right-[20.83%] top-[20.83%]" data-name="Vector">
        <div className="absolute inset-[-7.14%_-14.29%]">
          <img className="block max-w-none size-full" src={imgVector9} />
        </div>
      </div>
    </div>
  );
}

function Svg26() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame19 />
    </div>
  );
}

function SvgMargin19() {
  return (
    <div className="absolute box-border content-stretch flex flex-col h-3.5 items-start justify-start left-[110.15px] pl-[7px] pr-0 py-0 top-[10.5px] w-[21px]" data-name="SVG:margin">
      <Svg26 />
    </div>
  );
}

function Button5() {
  return (
    <div className="bg-white h-[35px] relative rounded-[6.75px] shrink-0 w-[145.16px]" data-name="Button">
      <div className="absolute flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] h-[18px] justify-center leading-[0] not-italic text-[#155dfc] text-[12.3px] text-center translate-x-[-50%] translate-y-[-50%] w-[89.36px]" style={{ top: "calc(50% - 0.75px)", left: "calc(50% - 13.9px)" }}>
        <p className="leading-[17.5px]">무료로 시작하기</p>
      </div>
      <SvgMargin19 />
    </div>
  );
}

function Button6() {
  return (
    <div className="bg-white box-border content-stretch flex h-[35px] items-center justify-center pb-[9.25px] pt-[7.75px] px-[22px] relative rounded-[6.75px] shrink-0" data-name="Button">
      <div aria-hidden="true" className="absolute border border-solid border-white inset-0 pointer-events-none rounded-[6.75px]" />
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-center text-nowrap text-white">
        <p className="leading-[17.5px] whitespace-pre">로그인</p>
      </div>
    </div>
  );
}

function Container106() {
  return (
    <div className="content-stretch flex gap-[13.99px] items-start justify-center relative shrink-0 w-full" data-name="Container">
      <Button5 />
      <Button6 />
    </div>
  );
}

function Container107() {
  return (
    <div className="content-stretch flex flex-col gap-[21px] items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Heading17 />
      <Container105 />
      <Container106 />
    </div>
  );
}

function Section5() {
  return (
    <div className="absolute bg-gradient-to-r box-border content-stretch flex flex-col from-[#155dfc] items-start justify-start left-0 px-[596px] py-[70px] right-0 to-[#9810fa] top-[2680.13px]" data-name="Section">
      <Container107 />
    </div>
  );
}

function Container108() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-nowrap text-white">
        <p className="leading-[17.5px] whitespace-pre">R</p>
      </div>
    </div>
  );
}

function Background10() {
  return (
    <div className="bg-gradient-to-r box-border content-stretch flex from-[#155dfc] items-center justify-center pb-[5.25px] pt-[4.25px] px-0 relative rounded-[6.75px] shrink-0 size-7 to-[#9810fa]" data-name="Background">
      <Container108 />
    </div>
  );
}

function Margin13() {
  return (
    <div className="box-border content-stretch flex flex-col h-7 items-start justify-start pl-0 pr-[7px] py-0 relative shrink-0 w-[35px]" data-name="Margin">
      <Background10 />
    </div>
  );
}

function Container109() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[15.8px] text-nowrap text-white">
        <p className="leading-[24.5px] whitespace-pre">ReBalance Pro</p>
      </div>
    </div>
  );
}

function Container110() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <Margin13 />
      <Container109 />
    </div>
  );
}

function Container111() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[21px] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="mb-0">AI 기반 포트폴리오 자동 리밸런싱으로</p>
        <p>더 스마트한 투자를 시작하세요.</p>
      </div>
    </div>
  );
}

function Container112() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-3.5 grow items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Container110 />
      <Container111 />
    </div>
  );
}

function Heading4() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 4">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-white w-full">
        <p className="leading-[21px]">제품</p>
      </div>
    </div>
  );
}

function Item15() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">기능</p>
      </div>
    </div>
  );
}

function Item16() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">요금제</p>
      </div>
    </div>
  );
}

function Item17() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">API</p>
      </div>
    </div>
  );
}

function Item18() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">모바일 앱</p>
      </div>
    </div>
  );
}

function List3() {
  return (
    <div className="content-stretch flex flex-col gap-[7px] items-start justify-start relative shrink-0 w-full" data-name="List">
      <Item15 />
      <Item16 />
      <Item17 />
      <Item18 />
    </div>
  );
}

function Container113() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-3.5 grow items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Heading4 />
      <List3 />
    </div>
  );
}

function Heading18() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 4">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-white w-full">
        <p className="leading-[21px]">지원</p>
      </div>
    </div>
  );
}

function Item19() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">도움말</p>
      </div>
    </div>
  );
}

function Item20() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">커뮤니티</p>
      </div>
    </div>
  );
}

function Item21() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">고객센터</p>
      </div>
    </div>
  );
}

function Item22() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">피드백</p>
      </div>
    </div>
  );
}

function List4() {
  return (
    <div className="content-stretch flex flex-col gap-[7px] items-start justify-start relative shrink-0 w-full" data-name="List">
      <Item19 />
      <Item20 />
      <Item21 />
      <Item22 />
    </div>
  );
}

function Container114() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-3.5 grow items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Heading18 />
      <List4 />
    </div>
  );
}

function Heading19() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 4">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-white w-full">
        <p className="leading-[21px]">회사</p>
      </div>
    </div>
  );
}

function Item23() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">소개</p>
      </div>
    </div>
  );
}

function Item24() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">채용</p>
      </div>
    </div>
  );
}

function Item25() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">블로그</p>
      </div>
    </div>
  );
}

function Item26() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Item">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] w-full">
        <p className="leading-[21px]">언론</p>
      </div>
    </div>
  );
}

function List5() {
  return (
    <div className="content-stretch flex flex-col gap-[7px] items-start justify-start relative shrink-0 w-full" data-name="List">
      <Item23 />
      <Item24 />
      <Item25 />
      <Item26 />
    </div>
  );
}

function Container115() {
  return (
    <div className="basis-0 content-stretch flex flex-col gap-3.5 grow items-start justify-start min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <Heading19 />
      <List5 />
    </div>
  );
}

function Container116() {
  return (
    <div className="content-stretch flex gap-7 items-start justify-center relative shrink-0 w-full" data-name="Container">
      <Container112 />
      <Container113 />
      <Container114 />
      <Container115 />
    </div>
  );
}

function Container117() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">© 2024 ReBalance Pro. All rights reserved.</p>
      </div>
    </div>
  );
}

function Link() {
  return (
    <div className="basis-0 content-stretch flex flex-col grow items-start justify-start min-h-px min-w-px relative shrink-0" data-name="Link">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">개인정보처리방침</p>
      </div>
    </div>
  );
}

function LinkMargin() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-center pl-0 pr-[21px] py-0 relative self-stretch shrink-0" data-name="Link:margin">
      <Link />
    </div>
  );
}

function Link1() {
  return (
    <div className="basis-0 content-stretch flex flex-col grow items-start justify-start min-h-px min-w-px relative shrink-0" data-name="Link">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">이용약관</p>
      </div>
    </div>
  );
}

function LinkMargin1() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-center pl-0 pr-[21px] py-0 relative self-stretch shrink-0" data-name="Link:margin">
      <Link1 />
    </div>
  );
}

function Link2() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative self-stretch shrink-0" data-name="Link">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#99a1af] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">쿠키정책</p>
      </div>
    </div>
  );
}

function Container118() {
  return (
    <div className="content-stretch flex items-start justify-start relative shrink-0" data-name="Container">
      <LinkMargin />
      <LinkMargin1 />
      <Link2 />
    </div>
  );
}

function HorizontalBorder() {
  return (
    <div className="box-border content-stretch flex items-center justify-between pb-0 pt-[29px] px-0 relative shrink-0 w-full" data-name="HorizontalBorder">
      <div aria-hidden="true" className="absolute border-[#1e2939] border-[1px_0px_0px] border-solid inset-0 pointer-events-none" />
      <Container117 />
      <Container118 />
    </div>
  );
}

function Container119() {
  return (
    <div className="max-w-[1120px] relative shrink-0 w-full" data-name="Container">
      <div className="max-w-inherit relative size-full">
        <div className="box-border content-stretch flex flex-col gap-[42px] items-start justify-start max-w-inherit px-7 py-0 relative w-full">
          <Container116 />
          <HorizontalBorder />
        </div>
      </div>
    </div>
  );
}

function Footer() {
  return (
    <div className="absolute bg-[#101828] box-border content-stretch flex flex-col items-start justify-start left-0 px-[400px] py-14 right-0 top-[2956.63px]" data-name="Footer">
      <Container119 />
    </div>
  );
}

function Container120() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-nowrap text-white">
        <p className="leading-[17.5px] whitespace-pre">R</p>
      </div>
    </div>
  );
}

function Background11() {
  return (
    <div className="bg-gradient-to-r box-border content-stretch flex from-[#155dfc] items-center justify-center pb-[5.25px] pt-[4.25px] px-0 relative rounded-[6.75px] shrink-0 size-7 to-[#9810fa]" data-name="Background">
      <Container120 />
    </div>
  );
}

function Margin14() {
  return (
    <div className="box-border content-stretch flex flex-col h-7 items-start justify-start pl-0 pr-[7px] py-0 relative shrink-0 w-[35px]" data-name="Margin">
      <Background11 />
    </div>
  );
}

function Container121() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[15.8px] text-neutral-950 text-nowrap">
        <p className="leading-[24.5px] whitespace-pre">ReBalance Pro</p>
      </div>
    </div>
  );
}

function Container122() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <Margin14 />
      <Container121 />
    </div>
  );
}

function LinkMargin2() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-7 py-0 relative shrink-0" data-name="Link:margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">기능</p>
      </div>
    </div>
  );
}

function LinkMargin3() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-7 py-0 relative shrink-0" data-name="Link:margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">사용법</p>
      </div>
    </div>
  );
}

function LinkMargin4() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pl-0 pr-7 py-0 relative shrink-0" data-name="Link:margin">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">요금제</p>
      </div>
    </div>
  );
}

function Link3() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Link">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-nowrap">
        <p className="leading-[21px] whitespace-pre">후기</p>
      </div>
    </div>
  );
}

function Nav() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Nav">
      <LinkMargin2 />
      <LinkMargin3 />
      <LinkMargin4 />
      <Link3 />
    </div>
  );
}

function Button7() {
  return (
    <div className="box-border content-stretch flex h-[31.5px] items-center justify-center pb-[7.5px] pt-1.5 px-3.5 relative rounded-[6.75px] shrink-0" data-name="Button">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-center text-neutral-950 text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">로그인</p>
      </div>
    </div>
  );
}

function ButtonMargin() {
  return (
    <div className="box-border content-stretch flex flex-col h-[31.5px] items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0" data-name="Button:margin">
      <Button7 />
    </div>
  );
}

function Button8() {
  return (
    <div className="bg-[#030213] box-border content-stretch flex h-[31.5px] items-center justify-center pb-[7.5px] pt-1.5 px-3.5 relative rounded-[6.75px] shrink-0" data-name="Button">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-center text-nowrap text-white">
        <p className="leading-[17.5px] whitespace-pre">무료로 시작하기</p>
      </div>
    </div>
  );
}

function Container123() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <ButtonMargin />
      <Button8 />
    </div>
  );
}

function Container124() {
  return (
    <div className="h-14 relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-row items-center relative size-full">
        <div className="box-border content-stretch flex h-14 items-center justify-between pl-0 pr-[0.01px] py-0 relative w-full">
          <Container122 />
          <Nav />
          <Container123 />
        </div>
      </div>
    </div>
  );
}

function Header() {
  return (
    <div className="backdrop-blur-[6px] backdrop-filter bg-[rgba(255,255,255,0.8)] box-border content-stretch flex flex-col items-start justify-start pb-px pointer-events-auto pt-0 px-[428px] sticky top-0" data-name="Header">
      <div aria-hidden="true" className="absolute border-[0px_0px_1px] border-gray-100 border-solid inset-0 pointer-events-none" />
      <Container124 />
    </div>
  );
}

export default function Component2() {
  return (
    <div className="bg-white relative size-full" data-name="랜딩페이지">
      <Section />
      <Section1 />
      <Section2 />
      <Section3 />
      <Section4 />
      <Section5 />
      <Footer />
      <div className="absolute inset-0 pointer-events-none">
        <Header />
      </div>
    </div>
  );
}