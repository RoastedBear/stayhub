import { chromium } from 'playwright'
import { readFileSync } from 'fs'

const ACCESS_TOKEN = readFileSync('./test_token.txt', 'utf-8').trim()
const browser = await chromium.launch({ headless: true })
const page = await browser.newPage()

const apiCalls = []

// 모든 /api/reservations 요청/응답 캡처
page.on('request', req => {
  if (req.url().includes('/api/reservations') && req.method() === 'POST') {
    apiCalls.push({ type: 'request', body: req.postData(), url: req.url() })
  }
})
page.on('response', async res => {
  if (res.url().includes('/api/reservations') && res.request().method() === 'POST') {
    try {
      const body = await res.json()
      apiCalls.push({ type: 'response', status: res.status(), body })
    } catch {
      apiCalls.push({ type: 'response', status: res.status(), body: await res.text() })
    }
  }
})

// localStorage에 토큰 주입
await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' })
await page.evaluate((token) => localStorage.setItem('accessToken', token), ACCESS_TOKEN)
await page.reload({ waitUntil: 'networkidle' })

// 숙소 상세 페이지 (acc_id=1, 재시작 후 새 숙소)
await page.goto('http://localhost:5173/accommodations/1', { waitUntil: 'networkidle' })
await page.screenshot({ path: 'ss_acc_detail.png' })

// 페이지에 표시된 텍스트 확인
const bodyText = await page.evaluate(() => document.body.innerText.substring(0, 500))
console.log(`페이지 내용: ${bodyText}`)

const roomBtns = await page.$$eval('button', btns =>
  btns.map(b => b.textContent.trim()).filter(t => t.includes('예약'))
)
console.log(`예약 관련 버튼: ${JSON.stringify(roomBtns)}`)

if (roomBtns.length > 0) {
  // 예약하기 버튼 클릭
  await page.click('button:has-text("예약하기")')
  await page.waitForURL('**/reservations/new**', { timeout: 5000 }).catch(() => {})
  const currentUrl = page.url()
  await page.screenshot({ path: 'ss_reservation.png' })
  console.log(`예약 페이지 URL: ${currentUrl}`)

  // URL에서 roomId 파라미터 확인
  const urlObj = new URL(currentUrl)
  console.log(`roomId 파라미터: ${urlObj.searchParams.get('roomId')}`)
  console.log(`roomName 파라미터: ${urlObj.searchParams.get('roomName')}`)
  console.log(`basePrice 파라미터: ${urlObj.searchParams.get('basePrice')}`)

  if (currentUrl.includes('/reservations/new')) {
    // 날짜 입력 (오늘+2, 오늘+4)
    const d1 = new Date(); d1.setDate(d1.getDate() + 2)
    const d2 = new Date(); d2.setDate(d2.getDate() + 4)
    const fmt = d => d.toISOString().split('T')[0]

    // index 방식으로 날짜 input 채우기
    const dateInputs = await page.$$('input[type="date"]')
    console.log(`날짜 input 개수: ${dateInputs.length}`)
    if (dateInputs[0]) await dateInputs[0].fill(fmt(d1))
    if (dateInputs[1]) await dateInputs[1].fill(fmt(d2))
    await page.screenshot({ path: 'ss_reservation_filled.png' })
    console.log(`체크인: ${fmt(d1)}, 체크아웃: ${fmt(d2)}`)

    // 야간 수 계산 확인
    const nightsText = await page.$eval('.price-summary', el => el.textContent.trim()).catch(() => '가격 계산 없음')
    console.log(`가격 계산: ${nightsText}`)

    // 제출 버튼 상태 확인
    const submitDisabled = await page.$eval('button[type="submit"]', b => b.disabled)
    console.log(`제출 버튼 비활성화: ${submitDisabled}`)

    // 제출
    await page.click('button[type="submit"]')
    await page.waitForTimeout(3000)
    await page.screenshot({ path: 'ss_reservation_after.png' })

    const afterUrl = page.url()
    console.log(`제출 후 URL: ${afterUrl}`)
  }
} else {
  console.log('예약하기 버튼을 찾을 수 없음')
}

console.log('\n--- API 호출 로그 ---')
for (const c of apiCalls) {
  if (c.type === 'request') {
    console.log(`요청 URL: ${c.url}`)
    console.log(`요청 바디: ${c.body}`)
  } else {
    console.log(`응답 상태: ${c.status}`)
    console.log(`응답 바디: ${JSON.stringify(c.body)}`)
  }
}

if (apiCalls.length === 0) {
  console.log('API 호출이 없었습니다.')
}

await browser.close()
