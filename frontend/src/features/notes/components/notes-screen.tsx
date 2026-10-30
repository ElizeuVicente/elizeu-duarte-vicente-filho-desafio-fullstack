"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Activity, ChevronDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, Download, FileText, House, LayoutDashboard, MapPin, MessageCircle, Pencil, Plus, ScrollText, SlidersHorizontal, Trash2, X } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { useCreateNote, useDeleteNote, useNotes, useUpdateNote } from "../hooks/use-notes";
import { noteFormSchema, type NoteFormValues } from "../schemas/note-schema";
import { exportNotes } from "../services/notes-service";
import type { Note, NotesFilters } from "../types/note";
import styles from "./notes-screen.module.css";

const initialFilters: NotesFilters = { site: "", equipment: "", startDate: "", endDate: "", page: 0, size: 5 };
const navigation = [
  { label: "Home", icon: House }, { label: "Análises", icon: Activity },
  { label: "Notas", icon: MessageCircle }, { label: "Histórico", icon: ScrollText },
  { label: "Mapa", icon: MapPin }, { label: "Dashboard", icon: LayoutDashboard },
];

function dateTime(value: string) {
  return new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "medium" }).format(new Date(value));
}

function toDateTimeLocal(value: string) {
  const date = new Date(value);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60_000).toISOString().slice(0, 16);
}

export function NotesScreen() {
  const [filters, setFilters] = useState(initialFilters);
  const [draft, setDraft] = useState(initialFilters);
  const [periodOpen, setPeriodOpen] = useState(false);
  const [filterError, setFilterError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingNote, setEditingNote] = useState<Note | null>(null);
  const [isExporting, setIsExporting] = useState(false);
  const [exportError, setExportError] = useState<string | null>(null);
  const { data, isPending, isFetching, error } = useNotes(filters);
  const createMutation = useCreateNote();
  const updateMutation = useUpdateNote();
  const deleteMutation = useDeleteNote();
  const form = useForm<NoteFormValues>({ resolver: zodResolver(noteFormSchema), defaultValues: { site: "", equipment: "", variable: "", timestamp: "", author: "", message: "" } });

  const closeModal = () => { form.reset(); setEditingNote(null); setModalOpen(false); };
  const openCreateModal = () => { form.reset(); setEditingNote(null); setModalOpen(true); };
  const openEditModal = (note: Note) => { setEditingNote(note); form.reset({ ...note, timestamp: toDateTimeLocal(note.timestamp) }); setModalOpen(true); };
  const submitNote = form.handleSubmit((values) => {
    const payload = { ...values, timestamp: new Date(values.timestamp).toISOString() };
    if (editingNote) updateMutation.mutate({ id: editingNote.id, note: payload }, { onSuccess: closeModal });
    else createMutation.mutate(payload, { onSuccess: closeModal });
  });
  const removeNote = (note: Note) => {
    if (window.confirm(`Excluir a nota de ${note.site}? Esta ação não pode ser desfeita.`)) deleteMutation.mutate(note.id);
  };
  const applyFilters = () => {
    if (draft.startDate && draft.endDate && draft.endDate < draft.startDate) {
      setFilterError("A data final deve ser igual ou posterior à inicial.");
      return;
    }
    setFilterError(null);
    setPeriodOpen(false);
    setFilters({ ...draft, page: 0 });
  };
  const updatePage = (page: number) => setFilters((current) => ({ ...current, page }));
  const downloadExport = async () => {
    setExportError(null); setIsExporting(true);
    try {
      const { blob, filename } = await exportNotes(filters);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url; link.download = filename; link.click();
      setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (failure) { setExportError(failure instanceof Error ? failure.message : "Não foi possível exportar as notas."); }
    finally { setIsExporting(false); }
  };

  const mutationError = createMutation.error ?? updateMutation.error;
  const isSaving = createMutation.isPending || updateMutation.isPending;
  const totalPages = data?.totalPages ?? 0;
  const currentPage = data?.number ?? 0;
  const pageStart = Math.max(0, Math.min(currentPage - 2, totalPages - 5));
  const visiblePages = Array.from({ length: Math.min(5, totalPages) }, (_, index) => pageStart + index);

  return <main className={styles.shell}>
    <aside className={styles.sidebar}>
      <div className={styles.brand} aria-label="Oxyn">
        <svg width="50" height="39" viewBox="0 0 50 39" role="img" aria-hidden="true">
          <path d="M16.4992 2H37.5808L22.0816 24.9729H1L16.4992 2Z" fill="#42A566" />
          <path d="M17.4226 27.1022L11.4194 36.0002H33.501L49.0002 13.0273H32.7026L23.2066 27.1022H17.4226Z" fill="#328751" />
        </svg>
      </div>
      <p className={styles.groupLabel}>MENU PRINCIPAL</p>
      <nav className={styles.navigation} aria-label="Menu principal">
        {navigation.map(({ label, icon: Icon }) => <button key={label} type="button" className={`${styles.navItem} ${label === "Notas" ? styles.active : ""}`} aria-current={label === "Notas" ? "page" : undefined} title={label}><Icon size={18} strokeWidth={2} /><span>{label}</span></button>)}
      </nav>
      <div className={styles.options}><p className={styles.groupLabel}>OPÇÕES</p><button className={styles.navItem} type="button" title="Logs"><FileText size={18} strokeWidth={2} /><span>Logs</span></button></div>
      <span className={styles.version}>© 2024 OXYN | v1.0.0</span>
    </aside>

    <section className={styles.content}>
      <header className={styles.topbar}>
        <div className={styles.user}>Elias <span>EG</span></div>
        <div className={styles.titleRow}>
          <h1>Notas</h1>
          <div className={styles.headerActions}>
            <button className={styles.secondaryButton} onClick={downloadExport} disabled={isExporting}><Download size={16} />{isExporting ? "Exportando…" : "Exportar CSV"}</button>
            <button className={styles.primaryButton} onClick={openCreateModal}><Plus size={17} />Nova nota</button>
          </div>
        </div>
      </header>

      <div className={styles.controls}>
        <label className={styles.controlField}><span className={styles.visuallyHidden}>Filtrar por site</span><input value={draft.site} onChange={(event) => setDraft({ ...draft, site: event.target.value })} placeholder="Selecione um site" onKeyDown={(event) => { if (event.key === "Enter") applyFilters(); }} /><ChevronDown size={19} aria-hidden="true" /></label>
        <label className={styles.controlField}><span className={styles.visuallyHidden}>Filtrar por equipamento</span><input value={draft.equipment} onChange={(event) => setDraft({ ...draft, equipment: event.target.value })} placeholder="Selecione um equipamento" onKeyDown={(event) => { if (event.key === "Enter") applyFilters(); }} /><ChevronDown size={19} aria-hidden="true" /></label>
        <div className={styles.periodControl}>
          <button className={styles.periodButton} type="button" aria-expanded={periodOpen} aria-controls="period-filters" onClick={() => setPeriodOpen(!periodOpen)}><SlidersHorizontal size={17} />Filtrar por período</button>
          {periodOpen && <div className={styles.periodPopover} id="period-filters">
            <label>Data inicial<input type="date" value={draft.startDate} onChange={(event) => setDraft({ ...draft, startDate: event.target.value })} /></label>
            <label>Data final<input type="date" value={draft.endDate} onChange={(event) => setDraft({ ...draft, endDate: event.target.value })} /></label>
            {filterError && <p className={styles.fieldError}>{filterError}</p>}
            <button className={styles.primaryButton} type="button" onClick={applyFilters}>Aplicar filtros</button>
          </div>}
        </div>
      </div>
      {(exportError || deleteMutation.error) && <p className={styles.bannerError} role="alert">{exportError ?? deleteMutation.error?.message}</p>}

      <div className={styles.tableWrap}>
        <div className={styles.table} role="table" aria-label="Notas">
          <div className={`${styles.row} ${styles.heading}`} role="row"><span role="columnheader">Site</span><span role="columnheader">Equipamento</span><span role="columnheader">Monitoração</span><span role="columnheader">Data</span><span role="columnheader">Autor</span><span role="columnheader">Mensagem</span><span role="columnheader" className={styles.visuallyHidden}>Ações</span></div>
          {isPending ? <div className={styles.status}>Carregando notas…</div> : error ? <div className={`${styles.status} ${styles.error}`} role="alert">{error.message}</div> : data?.content.map((note) => <div className={styles.row} role="row" key={note.id}>
            <span role="cell">{note.site}</span><span role="cell">{note.equipment}</span><span role="cell">{note.variable}</span><span role="cell">{dateTime(note.timestamp)}</span><span role="cell">{note.author}</span><span role="cell">{note.message}</span>
            <span className={styles.rowActions} role="cell"><button onClick={() => openEditModal(note)} aria-label={`Editar nota de ${note.site}`} title="Editar nota"><Pencil size={16} /></button><button onClick={() => removeNote(note)} disabled={deleteMutation.isPending} aria-label={`Excluir nota de ${note.site}`} title="Excluir nota"><Trash2 size={16} /></button></span>
          </div>)}
          {!isPending && !error && data?.empty && <div className={styles.status}>Nenhuma nota encontrada para estes filtros.</div>}
        </div>
      </div>

      <footer className={styles.footer}>
        <span>{data?.totalElements ?? 0} nota(s) {isFetching && "· atualizando"}</span>
        <nav className={styles.pagination} aria-label="Páginas de notas">
          <button disabled={!data || data.first} onClick={() => updatePage(0)} aria-label="Primeira página"><ChevronsLeft size={18} /></button>
          <button disabled={!data || data.first} onClick={() => updatePage(filters.page - 1)} aria-label="Página anterior"><ChevronLeft size={18} /></button>
          {visiblePages.map((page) => <button key={page} className={page === currentPage ? styles.currentPage : ""} onClick={() => updatePage(page)} aria-current={page === currentPage ? "page" : undefined} aria-label={`Página ${page + 1}`}>{page + 1}</button>)}
          <button disabled={!data || data.last} onClick={() => updatePage(filters.page + 1)} aria-label="Próxima página"><ChevronRight size={18} /></button>
          <button disabled={!data || data.last} onClick={() => updatePage(totalPages - 1)} aria-label="Última página"><ChevronsRight size={18} /></button>
        </nav>
      </footer>
    </section>

    {modalOpen && <div className={styles.overlay} role="dialog" aria-modal="true" aria-label={editingNote ? "Editar nota" : "Nova nota"}>
      <form className={styles.modal} onSubmit={submitNote}>
        <div className={styles.modalHeader}><h2>{editingNote ? "Editar nota" : "Nova nota"}</h2><button type="button" onClick={closeModal} aria-label="Fechar"><X size={20} /></button></div>
        <div className={styles.formGrid}>
          {([['site', 'Site'], ['equipment', 'Equipamento'], ['variable', 'Variável'], ['timestamp', 'Data e hora'], ['author', 'Autor']] as const).map(([name, label]) => <label key={name}>{label}<input type={name === "timestamp" ? "datetime-local" : "text"} {...form.register(name)} aria-invalid={Boolean(form.formState.errors[name])} />{form.formState.errors[name] && <small>{form.formState.errors[name]?.message}</small>}</label>)}
          <label className={styles.messageField}>Mensagem<textarea rows={4} {...form.register("message")} aria-invalid={Boolean(form.formState.errors.message)} />{form.formState.errors.message && <small>{form.formState.errors.message.message}</small>}</label>
        </div>
        {mutationError && <p className={styles.fieldError} role="alert">{mutationError.message}</p>}
        <div className={styles.modalActions}><button type="button" className={styles.secondaryButton} onClick={closeModal}>Cancelar</button><button type="submit" className={styles.primaryButton} disabled={isSaving}>{isSaving ? "Salvando…" : editingNote ? "Salvar alterações" : "Salvar nota"}</button></div>
      </form>
    </div>}
  </main>;
}
