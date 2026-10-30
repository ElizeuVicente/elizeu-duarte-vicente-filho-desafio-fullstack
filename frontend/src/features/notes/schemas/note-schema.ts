import { z } from "zod";
const requiredText = (max: number, label: string) => z.string().trim().min(1, `${label} é obrigatório.`).max(max, `${label} deve ter no máximo ${max} caracteres.`);
export const noteFormSchema = z.object({ site: requiredText(150, "Site"), equipment: requiredText(100, "Equipamento"), variable: requiredText(100, "Variável"), timestamp: z.string().min(1, "Data e hora são obrigatórias."), author: requiredText(150, "Autor"), message: z.string().trim().min(1, "Mensagem é obrigatória.") });
export type NoteFormValues = z.infer<typeof noteFormSchema>;
