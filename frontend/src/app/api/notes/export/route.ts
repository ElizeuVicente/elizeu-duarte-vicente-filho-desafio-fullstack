import { NextRequest, NextResponse } from "next/server";
import { z } from "zod";

const filtersSchema = z.object({
  site: z.string().trim().max(150).optional(), equipment: z.string().trim().max(100).optional(),
  startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Data inicial inválida.").optional(), endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Data final inválida.").optional(),
}).superRefine(({ startDate, endDate }, ctx) => { if (startDate && endDate && startDate > endDate) ctx.addIssue({ code: "custom", message: "A data final deve ser posterior à inicial.", path: ["endDate"] }); });

function apiUrl(path: string) { const baseUrl = process.env.NOTES_API_URL; if (!baseUrl) throw new Error("NOTES_API_URL não foi configurada."); return new URL(path, baseUrl.endsWith("/") ? baseUrl : `${baseUrl}/`).toString(); }

export async function GET(request: NextRequest) {
  const parsed = filtersSchema.safeParse(Object.fromEntries(request.nextUrl.searchParams));
  if (!parsed.success) return NextResponse.json({ message: parsed.error.issues[0]?.message ?? "Parâmetros inválidos." }, { status: 400 });
  try {
    const query = new URLSearchParams(); Object.entries(parsed.data).forEach(([key, value]) => { if (value) query.set(key, value); });
    const response = await fetch(`${apiUrl("notes/export")}?${query}`, { cache: "no-store" });
    if (!response.ok) return NextResponse.json(await response.json().catch(() => ({ message: "Erro inesperado na API de notas." })), { status: response.status });
    return new NextResponse(response.body, { headers: { "Content-Type": response.headers.get("Content-Type") ?? "text/csv; charset=utf-8", "Content-Disposition": response.headers.get("Content-Disposition") ?? "attachment; filename=notes.csv" } });
  } catch (error) { return NextResponse.json({ message: error instanceof Error ? error.message : "Não foi possível conectar à API." }, { status: 502 }); }
}
