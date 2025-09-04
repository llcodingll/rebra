import imgReBalancePro from "figma:asset/d2daa75fe2b747eea9d2585ebcd97a1b43927977.png";
import { imgVector, imgVector1, imgSvg, imgSvg1, imgSvg2, imgSvg3, imgSvg4, imgSvg5 } from "./svg-oja5c";

function Container() {
  return (
    <div className="absolute inset-0 opacity-10" data-name="Container">
      <div className="absolute left-[35px] rounded-[3.35544e+07px] size-28 top-[35px]" data-name="Border">
        <div aria-hidden="true" className="absolute border border-[rgba(255,255,255,0.3)] border-solid inset-0 pointer-events-none rounded-[3.35544e+07px]" />
      </div>
      <div className="absolute bg-[rgba(255,255,255,0.1)] right-[70px] rounded-[3.35544e+07px] size-[70px] top-[140px]" data-name="Overlay" />
      <div className="absolute bottom-28 left-[70px] rounded-[3.35544e+07px] size-[140px]" data-name="Border">
        <div aria-hidden="true" className="absolute border border-[rgba(255,255,255,0.2)] border-solid inset-0 pointer-events-none rounded-[3.35544e+07px]" />
      </div>
      <div className="absolute bg-[rgba(255,255,255,0.05)] bottom-[35px] right-[35px] rounded-[3.35544e+07px] size-[84px]" data-name="Overlay" />
    </div>
  );
}

function Frame() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-3.5" data-name="Frame">
      <div className="absolute bottom-[20.83%] left-[20.83%] right-1/2 top-[20.83%]" data-name="Vector">
        <div className="absolute inset-[-7.14%_-14.29%]">
          <img className="block max-w-none size-full" src={imgVector} />
        </div>
      </div>
      <div className="absolute bottom-1/2 left-[20.83%] right-[20.83%] top-1/2" data-name="Vector">
        <div className="absolute inset-[-0.58px_-7.14%]">
          <img className="block max-w-none size-full" src={imgVector1} />
        </div>
      </div>
    </div>
  );
}

function Svg() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center overflow-clip relative shrink-0 size-3.5" data-name="SVG">
      <Frame />
    </div>
  );
}

function SvgMargin() {
  return (
    <div className="absolute box-border content-stretch flex flex-col h-3.5 items-start justify-start left-[10.5px] pl-0 pr-[7px] py-0 top-[8.75px] w-[21px]" data-name="SVG:margin">
      <Svg />
    </div>
  );
}

function Button() {
  return (
    <div className="absolute h-[31.5px] left-[-14px] rounded-[6.75px] top-0 w-[138.16px]" data-name="Button">
      <SvgMargin />
      <div className="absolute flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] h-[18px] justify-center leading-[0] not-italic text-[12.3px] text-center text-white translate-x-[-50%] translate-y-[-50%] w-[89.36px]" style={{ top: "calc(50% - 0.75px)", left: "calc(50% + 14.1px)" }}>
        <p className="leading-[17.5px]">홈으로 돌아가기</p>
      </div>
    </div>
  );
}

function Container1() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[17.5px] text-nowrap text-white">
        <p className="leading-[24.5px] whitespace-pre">R</p>
      </div>
    </div>
  );
}

function Overlay() {
  return (
    <div className="bg-[rgba(255,255,255,0.2)] content-stretch flex items-center justify-center relative rounded-[8.75px] shrink-0 size-[42px]" data-name="Overlay">
      <Container1 />
    </div>
  );
}

function Margin() {
  return (
    <div className="box-border content-stretch flex flex-col h-[42px] items-start justify-start pl-0 pr-[10.5px] py-0 relative shrink-0 w-[52.5px]" data-name="Margin">
      <Overlay />
    </div>
  );
}

function Container2() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[21px] text-nowrap text-white">
        <p className="leading-[28px] whitespace-pre">ReBalance Pro</p>
      </div>
    </div>
  );
}

function Container3() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Container">
      <Margin />
      <Container2 />
    </div>
  );
}

function Heading1() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 1">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[52.5px] not-italic relative shrink-0 text-[42px] text-white w-full">
        <p className="mb-0">투자의 미래를</p>
        <p>지금 시작하세요</p>
      </div>
    </div>
  );
}

function Container4() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[28.44px] not-italic relative shrink-0 text-[17.5px] text-blue-100 w-full">
        <p className="mb-0">AI가 관리하는 스마트한 포트폴리오로</p>
        <p>더 안전하고 수익성 높은 투자를 경험해보세요</p>
      </div>
    </div>
  );
}

function Container5() {
  return (
    <div className="content-stretch flex flex-col gap-3.5 items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Heading1 />
      <Container4 />
    </div>
  );
}

function Container6() {
  return (
    <div className="absolute content-stretch flex flex-col gap-[21px] items-start justify-start left-0 right-0 top-[59.5px]" data-name="Container">
      <Container3 />
      <Container5 />
    </div>
  );
}

function Container7() {
  return (
    <div className="h-[298.38px] relative shrink-0 w-full" data-name="Container">
      <Button />
      <Container6 />
    </div>
  );
}

function Svg1() {
  return (
    <div className="relative shrink-0 size-3.5" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg} />
    </div>
  );
}

function Overlay1() {
  return (
    <div className="bg-[rgba(255,255,255,0.2)] content-stretch flex items-center justify-center relative rounded-[8.75px] shrink-0 size-7" data-name="Overlay">
      <Svg1 />
    </div>
  );
}

function Margin1() {
  return (
    <div className="box-border content-stretch flex flex-col h-7 items-start justify-start pl-0 pr-3.5 py-0 relative shrink-0 w-[42px]" data-name="Margin">
      <Overlay1 />
    </div>
  );
}

function Heading3() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-nowrap text-white">
        <p className="leading-[21px] whitespace-pre">즉시 시작</p>
      </div>
    </div>
  );
}

function Container8() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[0.91px] pt-0 px-0 relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-blue-100 text-nowrap">
        <p className="leading-[19.91px] whitespace-pre">가입 후 바로 AI 리밸런싱 서비스를 이용할 수 있어요</p>
      </div>
    </div>
  );
}

function Container9() {
  return (
    <div className="content-stretch flex flex-col gap-[2.5px] items-start justify-start min-w-[283.83px] relative shrink-0" data-name="Container">
      <Heading3 />
      <Container8 />
    </div>
  );
}

function Container10() {
  return (
    <div className="content-stretch flex items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Margin1 />
      <Container9 />
    </div>
  );
}

function Svg2() {
  return (
    <div className="relative shrink-0 size-3.5" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg1} />
    </div>
  );
}

function Overlay2() {
  return (
    <div className="bg-[rgba(255,255,255,0.2)] content-stretch flex items-center justify-center relative rounded-[8.75px] shrink-0 size-7" data-name="Overlay">
      <Svg2 />
    </div>
  );
}

function Margin2() {
  return (
    <div className="box-border content-stretch flex flex-col h-7 items-start justify-start pl-0 pr-3.5 py-0 relative shrink-0 w-[42px]" data-name="Margin">
      <Overlay2 />
    </div>
  );
}

function Heading5() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-nowrap text-white">
        <p className="leading-[21px] whitespace-pre">안전한 보안</p>
      </div>
    </div>
  );
}

function Container11() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[0.91px] pt-0 px-0 relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-blue-100 text-nowrap">
        <p className="leading-[19.91px] whitespace-pre">은행급 보안 시스템으로 고객님의 정보를 안전하게 보호합니다</p>
      </div>
    </div>
  );
}

function Container12() {
  return (
    <div className="content-stretch flex flex-col gap-[2.5px] items-start justify-start min-w-[338.94px] relative shrink-0" data-name="Container">
      <Heading5 />
      <Container11 />
    </div>
  );
}

function Container13() {
  return (
    <div className="content-stretch flex items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Margin2 />
      <Container12 />
    </div>
  );
}

function Svg3() {
  return (
    <div className="relative shrink-0 size-3.5" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg2} />
    </div>
  );
}

function Overlay3() {
  return (
    <div className="bg-[rgba(255,255,255,0.2)] content-stretch flex items-center justify-center relative rounded-[8.75px] shrink-0 size-7" data-name="Overlay">
      <Svg3 />
    </div>
  );
}

function Margin3() {
  return (
    <div className="box-border content-stretch flex flex-col h-7 items-start justify-start pl-0 pr-3.5 py-0 relative shrink-0 w-[42px]" data-name="Margin">
      <Overlay3 />
    </div>
  );
}

function Heading6() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Heading 3">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-nowrap text-white">
        <p className="leading-[21px] whitespace-pre">무료 체험</p>
      </div>
    </div>
  );
}

function Container14() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[0.91px] pt-0 px-0 relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-blue-100 text-nowrap">
        <p className="leading-[19.91px] whitespace-pre">14일 무료 체험으로 모든 기능을 제한 없이 사용해보세요</p>
      </div>
    </div>
  );
}

function Container15() {
  return (
    <div className="content-stretch flex flex-col gap-[2.5px] items-start justify-start min-w-[306.97px] relative shrink-0" data-name="Container">
      <Heading6 />
      <Container14 />
    </div>
  );
}

function Container16() {
  return (
    <div className="content-stretch flex items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Margin3 />
      <Container15 />
    </div>
  );
}

function Container17() {
  return (
    <div className="content-stretch flex flex-col gap-[21px] items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Container10 />
      <Container13 />
      <Container16 />
    </div>
  );
}

function ReBalancePro() {
  return <div className="bg-[0%_49.95%] bg-no-repeat bg-size-[100%_100%] h-[504.3px] max-w-[336px] rounded-[14px] shadow-[0px_25px_50px_-12px_rgba(0,0,0,0.25)] shrink-0 w-full" data-name="ReBalance Pro 앱 미리보기" style={{ backgroundImage: `url('${imgReBalancePro}')` }} />;
}

function Container18() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col items-start justify-start pb-0 pt-7 px-[270px] relative w-full">
          <ReBalancePro />
        </div>
      </div>
    </div>
  );
}

function Background() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative self-stretch shrink-0" data-name="Background">
      <div className="overflow-clip relative size-full">
        <div className="box-border content-stretch flex flex-col items-start justify-between pb-[42.01px] pt-[42px] px-[42px] relative size-full">
          <Container />
          <Container7 />
          <Container17 />
          <Container18 />
        </div>
      </div>
    </div>
  );
}

function Heading4() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Heading 4">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[21px] text-center text-neutral-950 w-full">
        <p className="leading-[28px]">로그인</p>
      </div>
    </div>
  );
}

function Heading4Margin() {
  return (
    <div className="box-border content-stretch flex flex-col items-start justify-start pb-[3.5px] pt-0 px-0 relative shrink-0 w-full" data-name="Heading 4:margin">
      <Heading4 />
    </div>
  );
}

function Container19() {
  return (
    <div className="content-stretch flex flex-col items-center justify-start relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#4a5565] text-[14px] text-center w-full">
        <p className="leading-[21px]">계정에 로그인하여 포트폴리오를 관리하세요</p>
      </div>
    </div>
  );
}

function Container20() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col gap-[5.25px] items-start justify-start p-[21px] relative w-full">
          <Heading4Margin />
          <Container19 />
        </div>
      </div>
    </div>
  );
}

function Tab() {
  return (
    <div className="bg-white box-border content-stretch flex h-[25.5px] items-center justify-center pb-[4.5px] pl-[67.63px] pr-[67.62px] pt-[3px] relative rounded-[12.75px] shrink-0" data-name="Tab">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-center text-neutral-950 text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">로그인</p>
      </div>
    </div>
  );
}

function Tab1() {
  return (
    <div className="box-border content-stretch flex h-[25.5px] items-center justify-center pb-[4.5px] pt-[3px] px-[61.5px] relative rounded-[12.75px] shrink-0" data-name="Tab">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-center text-neutral-950 text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">회원가입</p>
      </div>
    </div>
  );
}

function Tablist() {
  return (
    <div className="bg-[#ececf0] h-[31.5px] relative rounded-[12.75px] shrink-0 w-full" data-name="Tablist">
      <div className="flex flex-row items-center justify-center relative size-full">
        <div className="box-border content-stretch flex h-[31.5px] items-center justify-center pb-[2.5px] pt-[3.5px] px-[3px] relative w-full">
          <Tab />
          <Tab1 />
        </div>
      </div>
    </div>
  );
}

function TablistMargin() {
  return (
    <div className="box-border content-stretch flex flex-col h-[52.5px] items-start justify-start pb-[21px] pt-0 px-0 relative shrink-0 w-full" data-name="Tablist:margin">
      <Tablist />
    </div>
  );
}

function Label() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Label">
      <div className="basis-0 flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] grow justify-center leading-[0] min-h-px min-w-px not-italic relative shrink-0 text-[12.3px] text-neutral-950">
        <p className="leading-[12.25px]">이메일</p>
      </div>
    </div>
  );
}

function Container21() {
  return (
    <div className="absolute bottom-[7.75px] box-border content-stretch flex flex-col items-start justify-start left-9 overflow-clip pb-0.5 pl-0 pr-[188.84px] pt-[3px] top-[6.75px]" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#717182] text-[12.3px] text-nowrap">
        <p className="leading-[normal] whitespace-pre">이메일을 입력하세요</p>
      </div>
    </div>
  );
}

function Container22() {
  return <div className="absolute bottom-[8.75px] left-9 top-[8.75px] w-[302.5px]" data-name="Container" />;
}

function Input() {
  return (
    <div className="bg-[#f3f3f5] h-[31.5px] overflow-clip relative rounded-[6.75px] shrink-0 w-full" data-name="Input">
      <Container21 />
      <Container22 />
    </div>
  );
}

function Svg4() {
  return (
    <div className="absolute left-[10.5px] size-3.5 top-1/2 translate-y-[-50%]" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg3} />
    </div>
  );
}

function Container23() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Input />
      <Svg4 />
    </div>
  );
}

function Container24() {
  return (
    <div className="content-stretch flex flex-col gap-[6.75px] items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Label />
      <Container23 />
    </div>
  );
}

function Label1() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0 w-full" data-name="Label">
      <div className="basis-0 flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] grow justify-center leading-[0] min-h-px min-w-px not-italic relative shrink-0 text-[12.3px] text-neutral-950">
        <p className="leading-[12.25px]">비밀번호</p>
      </div>
    </div>
  );
}

function Container25() {
  return (
    <div className="absolute bottom-[7.75px] box-border content-stretch flex flex-col items-start justify-start left-9 overflow-clip pb-0.5 pl-0 pr-[152.09px] pt-[3px] top-[6.75px]" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#717182] text-[12.3px] text-nowrap">
        <p className="leading-[normal] whitespace-pre">비밀번호를 입력하세요</p>
      </div>
    </div>
  );
}

function Container26() {
  return <div className="absolute bottom-[8.75px] left-9 top-[8.75px] w-[278px]" data-name="Container" />;
}

function Input1() {
  return (
    <div className="bg-[#f3f3f5] h-[31.5px] overflow-clip relative rounded-[6.75px] shrink-0 w-full" data-name="Input">
      <Container25 />
      <Container26 />
    </div>
  );
}

function Svg5() {
  return (
    <div className="absolute left-[10.5px] size-3.5 top-1/2 translate-y-[-50%]" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg4} />
    </div>
  );
}

function Svg6() {
  return (
    <div className="relative shrink-0 size-3.5" data-name="SVG">
      <img className="block max-w-none size-full" src={imgSvg5} />
    </div>
  );
}

function Button1() {
  return (
    <div className="absolute bottom-0 box-border content-stretch flex items-center justify-center px-[8.75px] py-0 right-0 rounded-[6.75px] top-0" data-name="Button">
      <Svg6 />
    </div>
  );
}

function Container27() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Input1 />
      <Svg5 />
      <Button1 />
    </div>
  );
}

function Container28() {
  return (
    <div className="content-stretch flex flex-col gap-[6.75px] items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Label1 />
      <Container27 />
    </div>
  );
}

function CheckboxMargin() {
  return (
    <div className="box-border content-stretch flex flex-col h-3.5 items-start justify-start pl-0 pr-[7px] py-0 relative shrink-0 w-[21px]" data-name="Checkbox:margin">
      <div className="bg-[#f3f3f5] relative rounded-[4px] shrink-0 size-3.5" data-name="Checkbox">
        <div aria-hidden="true" className="absolute border border-[rgba(0,0,0,0.1)] border-solid inset-0 pointer-events-none rounded-[4px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)]" />
      </div>
    </div>
  );
}

function Container29() {
  return (
    <div className="content-stretch flex items-center justify-start relative shrink-0" data-name="Container">
      <CheckboxMargin />
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[12.3px] text-neutral-950 text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">로그인 상태 유지</p>
      </div>
    </div>
  );
}

function Button2() {
  return (
    <div className="box-border content-stretch flex h-[31.5px] items-center justify-center pb-[7.5px] pt-1.5 px-0 relative rounded-[6.75px] shrink-0" data-name="Button">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[#030213] text-[12.3px] text-center text-nowrap">
        <p className="leading-[17.5px] whitespace-pre">비밀번호 찾기</p>
      </div>
    </div>
  );
}

function Container30() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-row items-center relative size-full">
        <div className="content-stretch flex items-center justify-between relative w-full">
          <Container29 />
          <Button2 />
        </div>
      </div>
    </div>
  );
}

function Button3() {
  return (
    <div className="bg-gradient-to-r from-[#155dfc] h-[31.5px] relative rounded-[6.75px] shrink-0 to-[#9810fa] w-full" data-name="Button">
      <div className="flex flex-row items-center justify-center relative size-full">
        <div className="box-border content-stretch flex h-[31.5px] items-center justify-center pb-[7.5px] pt-1.5 px-3.5 relative w-full">
          <div className="basis-0 flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] grow justify-center leading-[0] min-h-px min-w-px not-italic relative shrink-0 text-[12.3px] text-center text-white">
            <p className="leading-[17.5px]">로그인</p>
          </div>
        </div>
      </div>
    </div>
  );
}

function TabpanelForm() {
  return (
    <div className="content-stretch flex flex-col gap-3.5 items-start justify-start relative shrink-0 w-full" data-name="Tabpanel → Form">
      <Container24 />
      <Container28 />
      <Container30 />
      <Button3 />
    </div>
  );
}

function Container31() {
  return (
    <div className="content-stretch flex flex-col gap-[7px] items-start justify-start relative shrink-0 w-full" data-name="Container">
      <TablistMargin />
      <TabpanelForm />
    </div>
  );
}

function Container32() {
  return (
    <div className="absolute box-border content-stretch flex inset-0 items-center justify-center px-0 py-[8.25px]" data-name="Container">
      <div className="basis-0 grow h-px min-h-px min-w-px relative shrink-0" data-name="Horizontal Divider">
        <div aria-hidden="true" className="absolute border-[1px_0px_0px] border-gray-200 border-solid inset-0 pointer-events-none" />
      </div>
    </div>
  );
}

function Container33() {
  return <div className="h-[17.5px] shrink-0 w-full" data-name="Container" />;
}

function Container34() {
  return (
    <div className="content-stretch flex flex-col items-start justify-start relative shrink-0 w-full z-[1]" data-name="Container">
      <Container32 />
      <Container33 />
    </div>
  );
}

function Container35() {
  return (
    <div className="content-stretch flex flex-col gap-[21px] isolate items-start justify-start relative shrink-0 w-full" data-name="Container">
      <Container34 />
    </div>
  );
}

function Container36() {
  return (
    <div className="relative shrink-0 w-full" data-name="Container">
      <div className="relative size-full">
        <div className="box-border content-stretch flex flex-col gap-[21px] items-start justify-start pb-[21px] pt-0 px-[21px] relative w-full">
          <Container31 />
          <Container35 />
        </div>
      </div>
    </div>
  );
}

function BackgroundShadow() {
  return (
    <div className="bg-white box-border content-stretch flex flex-col gap-[21px] items-start justify-start overflow-clip relative rounded-[12.75px] shadow-[0px_20px_25px_-5px_rgba(0,0,0,0.1),0px_8px_10px_-6px_rgba(0,0,0,0.1)] shrink-0 w-full" data-name="Background+Shadow">
      <Container20 />
      <Container36 />
    </div>
  );
}

function Container37() {
  return (
    <div className="box-border content-stretch flex flex-col items-center justify-start pb-[0.75px] pt-0 px-0 relative shrink-0 w-full" data-name="Container">
      <div className="flex flex-col font-['Segoe_UI_Symbol:Regular',_'Noto_Sans_KR:Regular',_sans-serif] justify-center leading-[17.5px] not-italic relative shrink-0 text-[#4a5565] text-[12.3px] text-center w-full">
        <p className="mb-0">
          <span>{`계정을 생성하면 `}</span>
          <span className="text-[#155dfc]">이용약관</span>
          <span>{`과 `}</span>
          <span className="text-[#155dfc]">개인정보처리방침</span>에 동의하는 것으로 간주
        </p>
        <p>됩니다.</p>
      </div>
    </div>
  );
}

function Container38() {
  return (
    <div className="content-stretch flex flex-col gap-[27.25px] items-start justify-start max-w-[392px] relative shrink-0 w-[392px]" data-name="Container">
      <BackgroundShadow />
      <Container37 />
    </div>
  );
}

function Container39() {
  return (
    <div className="basis-0 grow min-h-px min-w-px relative self-stretch shrink-0" data-name="Container">
      <div className="flex flex-row items-center justify-center relative size-full">
        <div className="box-border content-stretch flex items-center justify-center p-[42px] relative size-full">
          <Container38 />
        </div>
      </div>
    </div>
  );
}

export default function Component() {
  return (
    <div className="bg-white content-stretch flex items-start justify-start relative size-full" data-name="로그인">
      <Background />
      <Container39 />
    </div>
  );
}