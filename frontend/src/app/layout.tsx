import type { Metadata } from "next";
import { Sora } from "next/font/google";
import "./globals.css";
import { AppProviders } from "./providers";

const sora = Sora({ subsets: ["latin"], variable: "--font-sora" });
export const metadata: Metadata = { title: "Oxyn | Notas", description: "Monitoramento de notas operacionais" };

export default function RootLayout({ children }: LayoutProps<"/">) {
  return <html lang="pt-BR" className={sora.variable}><body><AppProviders>{children}</AppProviders></body></html>;
}
