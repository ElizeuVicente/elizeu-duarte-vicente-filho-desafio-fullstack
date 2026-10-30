import { NextRequest, NextResponse } from "next/server";
import { z } from "zod";

const filtersSchema = z.object({
  site: z.string().trim().max(150).optional(), equipment: z.string().trim().max(100).optional(),
  startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Data inicial inválida.").optional(),
  endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Data final inválida.").optional(),
  page: z.coerce.number().int().min(0).default(0), size: z.coerce.number().int().min(1).max(100).default(10),
}).superRefine(({ startDate, endDate }, ctx) => { if (startDate && endDate && startDate > endDate) ctx.addIssue({ code: "custom", message: "A data final deve ser posterior à inicial.", path: ["endDate"] }); });

const noteSchema = z.object({
  site: z.string().trim().min(1).max(150), equipment: z.string().trim().min(1).max(100), variable: z.string().trim().min(1).max(100),
  timestamp: z.string().datetime({ offset: true }), author: z.string().trim().min(1).max(150), message: z.string().trim().min(1),
});

function apiUrl(path: string) { const baseUrl = process.env.NOTES_API_URL; if (!baseUrl) throw new Error("NOTES_API_URL não foi configurada."); return new URL(path, baseUrl.endsWith("/") ? baseUrl : `${baseUrl}/`).toString(); }
async function proxyError(response: Response) { const payload = await response.json().catch(() => ({ message: "Erro inesperado na API de notas." })); return NextResponse.json(payload, { status: response.status }); }

export async function GET(request: NextRequest) {
  const parsed = filtersSchema.safeParse(Object.fromEntries(request.nextUrl.searchParams));
  if (!parsed.success) return NextResponse.json({ message: parsed.error.issues[0]?.message ?? "Parâmetros inválidos." }, { status: 400 });
  try {
    const query = new URLSearchParams(); Object.entries(parsed.data).forEach(([key, value]) => { if (value !== undefined && value !== "") query.set(key, String(value)); });
    const response = await fetch(`${apiUrl("notes")}?${query}`, { cache: "no-store" });
    if (!response.ok) return proxyError(response); return NextResponse.json(await response.json());
  } catch (error) { return NextResponse.json({ message: error instanceof Error ? error.message : "Não foi possível conectar à API." }, { status: 502 }); }
}

export async function POST(request: NextRequest) {
  const parsed = noteSchema.safeParse(await request.json().catch(() => null));
  if (!parsed.success) return NextResponse.json({ message: parsed.error.issues[0]?.message ?? "Dados inválidos." }, { status: 400 });
  try {
    const response = await fetch(apiUrl("notes"), { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(parsed.data) });
    if (!response.ok) return proxyError(response); return NextResponse.json(await response.json(), { status: 201 });
  } catch (error) { return NextResponse.json({ message: error instanceof Error ? error.message : "Não foi possível conectar à API." }, { status: 502 }); }
}
