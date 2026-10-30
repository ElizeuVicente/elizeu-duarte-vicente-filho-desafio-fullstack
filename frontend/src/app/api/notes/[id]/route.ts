import { NextRequest, NextResponse } from "next/server";
import { z } from "zod";

const idSchema = z.string().uuid("Identificador da nota inválido.");
const noteSchema = z.object({
  site: z.string().trim().min(1).max(150), equipment: z.string().trim().min(1).max(100), variable: z.string().trim().min(1).max(100),
  timestamp: z.string().datetime({ offset: true }), author: z.string().trim().min(1).max(150), message: z.string().trim().min(1),
});

function apiUrl(path: string) { const baseUrl = process.env.NOTES_API_URL; if (!baseUrl) throw new Error("NOTES_API_URL não foi configurada."); return new URL(path, baseUrl.endsWith("/") ? baseUrl : `${baseUrl}/`).toString(); }
async function proxyError(response: Response) { const payload = await response.json().catch(() => ({ message: "Erro inesperado na API de notas." })); return NextResponse.json(payload, { status: response.status }); }
async function paramsId(context: RouteContext<"/api/notes/[id]">) { return idSchema.safeParse((await context.params).id); }

export async function GET(_request: NextRequest, context: RouteContext<"/api/notes/[id]">) {
  const id = await paramsId(context); if (!id.success) return NextResponse.json({ message: id.error.issues[0]?.message }, { status: 400 });
  try { const response = await fetch(apiUrl(`notes/${id.data}`), { cache: "no-store" }); if (!response.ok) return proxyError(response); return NextResponse.json(await response.json()); }
  catch (error) { return NextResponse.json({ message: error instanceof Error ? error.message : "Não foi possível conectar à API." }, { status: 502 }); }
}

export async function PUT(request: NextRequest, context: RouteContext<"/api/notes/[id]">) {
  const id = await paramsId(context); if (!id.success) return NextResponse.json({ message: id.error.issues[0]?.message }, { status: 400 });
  const note = noteSchema.safeParse(await request.json().catch(() => null)); if (!note.success) return NextResponse.json({ message: note.error.issues[0]?.message ?? "Dados inválidos." }, { status: 400 });
  try { const response = await fetch(apiUrl(`notes/${id.data}`), { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify(note.data) }); if (!response.ok) return proxyError(response); return NextResponse.json(await response.json()); }
  catch (error) { return NextResponse.json({ message: error instanceof Error ? error.message : "Não foi possível conectar à API." }, { status: 502 }); }
}

export async function DELETE(_request: NextRequest, context: RouteContext<"/api/notes/[id]">) {
  const id = await paramsId(context); if (!id.success) return NextResponse.json({ message: id.error.issues[0]?.message }, { status: 400 });
  try { const response = await fetch(apiUrl(`notes/${id.data}`), { method: "DELETE" }); if (!response.ok) return proxyError(response); return new NextResponse(null, { status: 204 }); }
  catch (error) { return NextResponse.json({ message: error instanceof Error ? error.message : "Não foi possível conectar à API." }, { status: 502 }); }
}
