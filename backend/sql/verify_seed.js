/** 校验 campushub.sql 的生成质量:行数、语法标记、唯一性、转义 */
const fs = require('fs')
const path = require('path')

const sql = fs.readFileSync(path.join(__dirname, 'campushub.sql'), 'utf8')
const lines = sql.split(/\r?\n/)

// 1. INSERT 语句与行数
const inserts = []
let current = null
for (const raw of lines) {
  const line = raw.trim()
  const match = /^INSERT INTO `(\w+)`/.exec(line)
  if (match) {
    current = { table: match[1], rows: 0, ok: true }
    inserts.push(current)
    continue
  }
  if (current && line.startsWith('(')) {
    current.rows += 1
    const isLast = line.endsWith(';')
    const isMid = line.endsWith(',')
    if (!isLast && !isMid) current.ok = false
  }
}

console.log('INSERT 语句数:', inserts.length)
for (const item of inserts) {
  console.log(`  ${item.table.padEnd(28)} ${String(item.rows).padStart(5)} 行  语法标记:${item.ok ? 'OK' : 'BAD'}`)
}

// 2. 报名 / 收藏唯一性
function extractPairs(tableName) {
  const block = sql.split(`INSERT INTO \`${tableName}\``)[1].split(';')[0]
  return [...block.matchAll(/\((\d+),\s*(\d+),/g)].map((m) => `${m[1]}-${m[2]}`)
}

const regPairs = extractPairs('campus_activity_registration')
const favPairs = extractPairs('campus_favorite')
console.log('报名行数:', regPairs.length, '唯一:', new Set(regPairs).size === regPairs.length)
console.log('收藏行数:', favPairs.length, '唯一:', new Set(favPairs).size === favPairs.length)

// 3. 用户用户名唯一
const userBlock = sql.split('INSERT INTO `campus_user`')[1].split(';')[0]
const usernames = [...userBlock.matchAll(/^  \('([^']+)',/gm)].map((m) => m[1])
console.log('用户行数:', usernames.length, '用户名唯一:', new Set(usernames).size === usernames.length)

// 4. 昵称唯一
const nicknames = [...userBlock.matchAll(/^  \('[^']+', '[^']+', '([^']+)',/gm)].map((m) => m[1])
console.log('昵称唯一:', new Set(nicknames).size === nicknames.length)

// 5. 孤立单引号(未成对转义)
let quoteCount = 0
for (const line of lines) {
  const noComment = line.replace(/^\s*--.*$/, '')
  const quoteHits = noComment.match(/[^']'[^']|^'[^']|[^']'$/g)
  if (quoteHits) quoteCount += quoteHits.length
}
console.log('可疑单引号片段数:', quoteCount)

// 6. 所有表都出现在 DDL 中
const tables = ['campus_user', 'campus_product_category', 'campus_product', 'campus_favorite',
  'campus_activity_category', 'campus_activity', 'campus_activity_registration',
  'campus_announcement', 'campus_lost_found', 'campus_notification']
for (const table of tables) {
  const hasDDL = new RegExp(`CREATE TABLE \`${table}\``).test(sql)
  const hasDML = new RegExp(`INSERT INTO \`${table}\``).test(sql)
  console.log(`${table}: DDL=${hasDDL ? 'OK' : 'MISS'} DML=${hasDML ? 'OK' : 'MISS'}`)
}
